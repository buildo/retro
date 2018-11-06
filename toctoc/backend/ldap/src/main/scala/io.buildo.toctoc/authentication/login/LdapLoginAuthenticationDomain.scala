package io.buildo.toctoc
package ldap
package authentication
package login

import core.authentication._
import core.authentication.TokenBasedAuthentication._
import com.unboundid.ldap.sdk.{LDAPConnection, LDAPException, ResultCode}

import scala.concurrent.{ExecutionContext, Future}

class LdapLoginAuthenticationDomain(ldapConfig: LdapConfig)(implicit ec: ExecutionContext)
  extends LoginAuthenticationDomain
  with BCryptHashing {

  override def register(s: Subject, c: Login): Future[Either[AuthenticationError, LoginDomain]] = ???

  override def unregister(s: Subject): Future[Either[AuthenticationError, LoginDomain]] = ???

  override def unregister(c: Login): Future[Either[AuthenticationError, LoginDomain]] = ???

  override def authenticate(c: Login): Future[Either[AuthenticationError, (LoginDomain, Subject)]] = Future {

    val username = buildUsername(c.username, ldapConfig)
    val host = ldapConfig.host
    val port = ldapConfig.port

    val conn = new LDAPConnection()
    try {
      conn.connect(host, port)
      conn.bind(username, c.password)

      Right((this, UserSubject(username)))
    } catch {
      case e: LDAPException => Left(fromResultCodeToLDAPError(e.getResultCode))
      case _: Exception => Left(AuthenticationError.LDAPGenericError)
    }
    finally {
      conn.close
    }
  }

  private[this] def buildUsername(username: String, ldapConfig: LdapConfig): String = {
    val loginType: LdapLoginType = ldapConfig.loginType
    val domain: String = ldapConfig.domain
    val distinguishedNameCommonName = ldapConfig.distinguishedNameCommonName

    loginType match {
      case LdapLoginType.UserPrincipalName => {
        // username example: mariorossi@corp.banksealer.com
        if (username.contains("@")) username
        else s"${username}@${domain}"
      }
      case LdapLoginType.DistinguishedName => {
        // username example: CN=Mario MR. Rossi,CN=Users,DC=corp,DC=banksealer,DC=com
        val customDomain = domain.split('.').map(el => s"DC=${el}").mkString(",")
        val commonName = s"CN=${distinguishedNameCommonName}"
        val customUsername = s"CN=${username}"
        s"${customUsername},${commonName},${customDomain}"
      }
      case LdapLoginType.Legacy => {
        // username example: CORP\mariorossi
        s"${domain}\\${username}"
      }
    }
  }

  // This function converts an LDAP result code to a toctoc authentication error
  private[this] def fromResultCodeToLDAPError(result: ResultCode): AuthenticationError = result match {
    case ResultCode.OPERATIONS_ERROR => AuthenticationError.LDAPOperationsError
    case ResultCode.PROTOCOL_ERROR => AuthenticationError.LDAPProtocolError
    case ResultCode.TIME_LIMIT_EXCEEDED => AuthenticationError.LDAPTimeLimitExceeded
    case ResultCode.SIZE_LIMIT_EXCEEDED => AuthenticationError.LDAPSizeLimitExceeded
    case ResultCode.COMPARE_FALSE => AuthenticationError.LDAPCompareFalse
    case ResultCode.COMPARE_TRUE => AuthenticationError.LDAPCompareTrue
    case ResultCode.AUTH_METHOD_NOT_SUPPORTED => AuthenticationError.LDAPAuthMethodNotSupported
    case ResultCode.STRONG_AUTH_REQUIRED => AuthenticationError.LDAPStrongAuthRequired
    case ResultCode.REFERRAL => AuthenticationError.LDAPReferral
    case ResultCode.ADMIN_LIMIT_EXCEEDED => AuthenticationError.LDAPAdminLimitExceeded
    case ResultCode.UNAVAILABLE_CRITICAL_EXTENSION => AuthenticationError.LDAPUnavailableCriticalExtension
    case ResultCode.CONFIDENTIALITY_REQUIRED => AuthenticationError.LDAPConfidentialityRequired
    case ResultCode.SASL_BIND_IN_PROGRESS => AuthenticationError.LDAPSaslBindInProgress
    case ResultCode.NO_SUCH_ATTRIBUTE => AuthenticationError.LDAPNoSuchAttribute
    case ResultCode.UNDEFINED_ATTRIBUTE_TYPE => AuthenticationError.LDAPUndefinedAttributeType
    case ResultCode.INAPPROPRIATE_MATCHING => AuthenticationError.LDAPInappropriateMatching
    case ResultCode.CONSTRAINT_VIOLATION => AuthenticationError.LDAPConstraintViolation
    case ResultCode.ATTRIBUTE_OR_VALUE_EXISTS => AuthenticationError.LDAPAttributeOrValueExists
    case ResultCode.INVALID_ATTRIBUTE_SYNTAX => AuthenticationError.LDAPInvalidAttributeSyntax
    case ResultCode.NO_SUCH_OBJECT => AuthenticationError.LDAPNoSuchObject
    case ResultCode.ALIAS_PROBLEM => AuthenticationError.LDAPAliasProblem
    case ResultCode.INVALID_DN_SYNTAX => AuthenticationError.LDAPInvalidDnSyntax
    case ResultCode.ALIAS_DEREFERENCING_PROBLEM => AuthenticationError.LDAPAliasDereferencingProblem
    case ResultCode.INAPPROPRIATE_AUTHENTICATION => AuthenticationError.LDAPInappropriateAuthentication
    case ResultCode.INVALID_CREDENTIALS => AuthenticationError.LDAPInvalidCredentials
    case ResultCode.INSUFFICIENT_ACCESS_RIGHTS => AuthenticationError.LDAPInsufficientAccessRights
    case ResultCode.BUSY => AuthenticationError.LDAPBusy
    case ResultCode.UNAVAILABLE => AuthenticationError.LDAPUnavailable
    case ResultCode.UNWILLING_TO_PERFORM => AuthenticationError.LDAPUnwillingToPerform
    case ResultCode.LOOP_DETECT => AuthenticationError.LDAPLoopDetect
    case ResultCode.SORT_CONTROL_MISSING => AuthenticationError.LDAPSortControlMissing
    case ResultCode.OFFSET_RANGE_ERROR => AuthenticationError.LDAPOffsetRangeError
    case ResultCode.NAMING_VIOLATION => AuthenticationError.LDAPNamingViolation
    case ResultCode.OBJECT_CLASS_VIOLATION => AuthenticationError.LDAPObjectClassViolation
    case ResultCode.NOT_ALLOWED_ON_NONLEAF => AuthenticationError.LDAPNotAllowedOnNonleaf
    case ResultCode.NOT_ALLOWED_ON_RDN => AuthenticationError.LDAPNotAllowedOnRdn
    case ResultCode.ENTRY_ALREADY_EXISTS => AuthenticationError.LDAPEntryAlreadyExists
    case ResultCode.OBJECT_CLASS_MODS_PROHIBITED => AuthenticationError.LDAPObjectClassModsProhibited
    case ResultCode.AFFECTS_MULTIPLE_DSAS => AuthenticationError.LDAPAffectsMultipleDsas
    case ResultCode.VIRTUAL_LIST_VIEW_ERROR => AuthenticationError.LDAPVirtualListViewError
    case ResultCode.OTHER => AuthenticationError.LDAPOther
    case ResultCode.SERVER_DOWN => AuthenticationError.LDAPServerDown
    case ResultCode.LOCAL_ERROR => AuthenticationError.LDAPLocalError
    case ResultCode.ENCODING_ERROR => AuthenticationError.LDAPEncodingError
    case ResultCode.DECODING_ERROR => AuthenticationError.LDAPDecodingError
    case ResultCode.TIMEOUT => AuthenticationError.LDAPTimeout
    case ResultCode.AUTH_UNKNOWN => AuthenticationError.LDAPAuthUnknown
    case ResultCode.FILTER_ERROR => AuthenticationError.LDAPFilterError
    case ResultCode.USER_CANCELED => AuthenticationError.LDAPUserCanceled
    case ResultCode.PARAM_ERROR => AuthenticationError.LDAPParamError
    case ResultCode.NO_MEMORY => AuthenticationError.LDAPNoMemory
    case ResultCode.CONNECT_ERROR => AuthenticationError.LDAPConnectError
    case ResultCode.NOT_SUPPORTED => AuthenticationError.LDAPNotSupported
    case ResultCode.CONTROL_NOT_FOUND => AuthenticationError.LDAPControlNotFound
    case ResultCode.NO_RESULTS_RETURNED => AuthenticationError.LDAPNoResultsReturned
    case ResultCode.MORE_RESULTS_TO_RETURN => AuthenticationError.LDAPMoreResultsToReturn
    case ResultCode.CLIENT_LOOP => AuthenticationError.LDAPClientLoop
    case ResultCode.REFERRAL_LIMIT_EXCEEDED => AuthenticationError.LDAPReferralLimitExceeded
    case ResultCode.CANCELED => AuthenticationError.LDAPCanceled
    case ResultCode.NO_SUCH_OPERATION => AuthenticationError.LDAPNoSuchOperation
    case ResultCode.TOO_LATE => AuthenticationError.LDAPTooLate
    case ResultCode.CANNOT_CANCEL => AuthenticationError.LDAPCannotCancel
    case ResultCode.ASSERTION_FAILED => AuthenticationError.LDAPAssertionFailed
    case ResultCode.AUTHORIZATION_DENIED => AuthenticationError.LDAPAuthorizationDenied
    case ResultCode.E_SYNC_REFRESH_REQUIRED => AuthenticationError.LDAPESyncRefreshRequired
    case ResultCode.NO_OPERATION => AuthenticationError.LDAPNoOperation
    case ResultCode.INTERACTIVE_TRANSACTION_ABORTED => AuthenticationError.LDAPInteractiveTransactionAborted
    case ResultCode.DATABASE_LOCK_CONFLICT => AuthenticationError.LDAPDatabaseLockConflict
    case ResultCode.MIRRORED_SUBTREE_DIGEST_MISMATCH => AuthenticationError.LDAPMirroredSubtreeDigestMismatch
    case ResultCode.TOKEN_DELIVERY_MECHANISM_UNAVAILABLE => AuthenticationError.LDAPTokenDeliveryMechanismUnavailable
    case ResultCode.TOKEN_DELIVERY_ATTEMPT_FAILED => AuthenticationError.LDAPTokenDeliveryAttemptFailed
    case ResultCode.TOKEN_DELIVERY_INVALID_RECIPIENT_ID => AuthenticationError.LDAPTokenDeliveryInvalidRecipientId
    case ResultCode.TOKEN_DELIVERY_INVALID_ACCOUNT_STATE => AuthenticationError.LDAPTokenDeliveryInvalidAccountState
    case _ => AuthenticationError.LDAPGenericError
  }
}
