package com.example.todo.security

import com.example.todo.config.JwtProperties
import com.example.todo.domain.user.User
import com.example.todo.exception.BadRequestException
import com.example.todo.exception.UnauthorizedException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component

@Component
class JwtTokenProvider(
        private val jwtProperties: JwtProperties,
        private val userDetailsService: UserDetailsService
) {
    companion object {
        const val CLAIM_UID = "uid"
        const val CLAIM_USERNAME = "uname"
        const val CLAIM_ROLES = "roles"
    }

    private val key: SecretKey by lazy {
        // If secret is too short, Keys.hmacShaKeyFor may throw; treat as server misconfig
        runCatching { Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray(StandardCharsets.UTF_8)) }
                .getOrElse {
                    throw IllegalStateException(
                            "JWT secret is invalid/misconfigured"
                    ) // usually 500
                }
    }

    fun generateAccessToken(user: User): String {
        val userId = user.id ?: throw IllegalStateException("User id is required to generate token")
        val now = Instant.now()
        val exp = now.plusSeconds(jwtProperties.accessTokenExpirationSeconds)

        val roles = user.roles.map { it.name }.toSet() // <-- add

        return Jwts.builder()
                .issuer(jwtProperties.issuer)
                .subject(user.email)
                .claim(CLAIM_UID, userId.toString())
                .claim(CLAIM_USERNAME, user.username)
                .claim(CLAIM_ROLES, roles) // <-- add
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact()
    }

    fun generateRefreshTokenValue(): String = UUID.randomUUID().toString()

    /**
     * Prefer "parse + throw" over boolean in request flow, so GlobalExceptionHandler can format the
     * error. If you still want a boolean check somewhere, keep validateToken().
     */
    fun validateToken(token: String): Boolean = runCatching { parseSignedClaims(token) }.isSuccess

    fun getClaims(token: String): Claims = parseSignedClaims(token).payload

    fun getEmail(token: String): String =
            getClaims(token).subject
                    ?: throw UnauthorizedException("Invalid token: missing subject")

    fun getUserId(token: String): UUID {
        val claims = getClaims(token)
        val v =
                claims[CLAIM_UID]
                        ?: throw UnauthorizedException("Invalid token: missing claim $CLAIM_UID")
        return runCatching { UUID.fromString(v.toString()) }.getOrElse {
            throw UnauthorizedException("Invalid token: bad user id")
        }
    }

    fun getAuthentication(token: String): Authentication {
        val claims = getClaims(token)

        val email = claims.subject ?: throw UnauthorizedException("Invalid token: missing subject")
        val userId =
                runCatching { UUID.fromString(claims[CLAIM_UID].toString()) }.getOrElse {
                    throw UnauthorizedException("Invalid token: bad user id")
                }

        val roles =
                (claims[CLAIM_ROLES] as? Collection<*>)?.mapNotNull { it?.toString() }
                        ?: emptyList()

        // passwordHash is not available in JWT; UserDetails allows empty/placeholder
        val principal =
                AuthUser(
                        id = userId,
                        email = email,
                        passwordHash = "", // placeholder; not used for JWT-authenticated requests
                        roles = roles
                )

        return UsernamePasswordAuthenticationToken(principal, null, principal.authorities)
    }
    /**
     * Centralized parsing so all JWT parsing errors become your ApiException types (handled by
     * GlobalExceptionHandler).
     */
    private fun parseSignedClaims(token: String) =
            try {
                // verify signature + parse claims (+ checks exp)
                Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
            } catch (e: ExpiredJwtException) {
                throw UnauthorizedException("Token expired")
            } catch (e: JwtException) {
                // Signature invalid, malformed, unsupported, etc.
                throw UnauthorizedException("Invalid token")
            } catch (e: IllegalArgumentException) {
                // null/blank token, etc.
                throw BadRequestException("Missing/invalid Authorization token")
            }
}
