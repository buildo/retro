package io.buildo.toctoc
package ldap
package authentication
package login

import core.authentication._
import core.authentication.TokenBasedAuthentication._
import com.unboundid.ldap.sdk.{LDAPConnection, LDAPException, ResultCode}

import scala.concurrent.{ExecutionContext, Future}

class LdapLoginAuthenticationDomain(host: String, port: Int)(implicit ec: ExecutionContext)
  extends LoginAuthenticationDomain
  with BCryptHashing {


  override def register(s: Subject, c: Login): Future[Either[AuthenticationError, LoginDomain]] = ???

  override def unregister(s: Subject): Future[Either[AuthenticationError, LoginDomain]] = ???

  override def unregister(c: Login): Future[Either[AuthenticationError, LoginDomain]] = ???

  override def authenticate(c: Login): Future[Either[AuthenticationError, (LoginDomain, Subject)]] = Future {
    val conn = new LDAPConnection()
    try {
      conn.connect(host, port)
      conn.bind(c.username, c.password)

      Right((this, UserSubject(c.username)))
    } catch {
      case e: LDAPException => fromResultCodeToLDAPError(e.getResultCode)
      case _: Exception => Left(AuthenticationError.LDAPGenericError)
    }
    finally {
      conn.close
    }
  }

  // This function converts an LDAP result code to a toctoc authentication error
  private[this] def fromResultCodeToLDAPError(result: ResultCode): Either[AuthenticationError, (LoginDomain, Subject)] = result match {
    case ResultCode.OPERATIONS_ERROR => Left(AuthenticationError.LDAPOperationsError)
    case ResultCode.PROTOCOL_ERROR => Left(AuthenticationError.LDAPProtocolError)
    case ResultCode.TIME_LIMIT_EXCEEDED => Left(AuthenticationError.LDAPTimeLimitExceeded)
    case ResultCode.SIZE_LIMIT_EXCEEDED => Left(AuthenticationError.LDAPSizeLimitExceeded)
    case ResultCode.COMPARE_FALSE => Left(AuthenticationError.LDAPCompareFalse)
    case ResultCode.COMPARE_TRUE => Left(AuthenticationError.LDAPCompareTrue)
    case ResultCode.AUTH_METHOD_NOT_SUPPORTED => Left(AuthenticationError.LDAPAuthMethodNotSupported)
    case ResultCode.STRONG_AUTH_REQUIRED => Left(AuthenticationError.LDAPStrongAuthRequired)
    case ResultCode.REFERRAL => Left(AuthenticationError.LDAPReferral)
    case ResultCode.ADMIN_LIMIT_EXCEEDED => Left(AuthenticationError.LDAPAdminLimitExceeded)
    case ResultCode.UNAVAILABLE_CRITICAL_EXTENSION => Left(AuthenticationError.LDAPUnavailableCriticalExtension)
    case ResultCode.CONFIDENTIALITY_REQUIRED => Left(AuthenticationError.LDAPConfidentialityRequired)
    case ResultCode.SASL_BIND_IN_PROGRESS => Left(AuthenticationError.LDAPSaslBindInProgress)
    case ResultCode.NO_SUCH_ATTRIBUTE => Left(AuthenticationError.LDAPNoSuchAttribute)
    case ResultCode.UNDEFINED_ATTRIBUTE_TYPE => Left(AuthenticationError.LDAPUndefinedAttributeType)
    case ResultCode.INAPPROPRIATE_MATCHING => Left(AuthenticationError.LDAPInappropriateMatching)
    case ResultCode.CONSTRAINT_VIOLATION => Left(AuthenticationError.LDAPConstraintViolation)
    case ResultCode.ATTRIBUTE_OR_VALUE_EXISTS => Left(AuthenticationError.LDAPAttributeOrValueExists)
    case ResultCode.INVALID_ATTRIBUTE_SYNTAX => Left(AuthenticationError.LDAPInvalidAttributeSyntax)
    case ResultCode.NO_SUCH_OBJECT => Left(AuthenticationError.LDAPNoSuchObject)
    case ResultCode.ALIAS_PROBLEM => Left(AuthenticationError.LDAPAliasProblem)
    case ResultCode.INVALID_DN_SYNTAX => Left(AuthenticationError.LDAPInvalidDnSyntax)
    case ResultCode.ALIAS_DEREFERENCING_PROBLEM => Left(AuthenticationError.LDAPAliasDereferencingProblem)
    case ResultCode.INAPPROPRIATE_AUTHENTICATION => Left(AuthenticationError.LDAPInappropriateAuthentication)
    case ResultCode.INVALID_CREDENTIALS => Left(AuthenticationError.LDAPInvalidCredentials)
    case ResultCode.INSUFFICIENT_ACCESS_RIGHTS => Left(AuthenticationError.LDAPInsufficientAccessRights)
    case ResultCode.BUSY => Left(AuthenticationError.LDAPBusy)
    case ResultCode.UNAVAILABLE => Left(AuthenticationError.LDAPUnavailable)
    case ResultCode.UNWILLING_TO_PERFORM => Left(AuthenticationError.LDAPUnwillingToPerform)
    case ResultCode.LOOP_DETECT => Left(AuthenticationError.LDAPLoopDetect)
    case ResultCode.SORT_CONTROL_MISSING => Left(AuthenticationError.LDAPSortControlMissing)
    case ResultCode.OFFSET_RANGE_ERROR => Left(AuthenticationError.LDAPOffsetRangeError)
    case ResultCode.NAMING_VIOLATION => Left(AuthenticationError.LDAPNamingViolation)
    case ResultCode.OBJECT_CLASS_VIOLATION => Left(AuthenticationError.LDAPObjectClassViolation)
    case ResultCode.NOT_ALLOWED_ON_NONLEAF => Left(AuthenticationError.LDAPNotAllowedOnNonleaf)
    case ResultCode.NOT_ALLOWED_ON_RDN => Left(AuthenticationError.LDAPNotAllowedOnRdn)
    case ResultCode.ENTRY_ALREADY_EXISTS => Left(AuthenticationError.LDAPEntryAlreadyExists)
    case ResultCode.OBJECT_CLASS_MODS_PROHIBITED => Left(AuthenticationError.LDAPObjectClassModsProhibited)
    case ResultCode.AFFECTS_MULTIPLE_DSAS => Left(AuthenticationError.LDAPAffectsMultipleDsas)
    case ResultCode.VIRTUAL_LIST_VIEW_ERROR => Left(AuthenticationError.LDAPVirtualListViewError)
    case ResultCode.OTHER => Left(AuthenticationError.LDAPOther)
    case ResultCode.SERVER_DOWN => Left(AuthenticationError.LDAPServerDown)
    case ResultCode.LOCAL_ERROR => Left(AuthenticationError.LDAPLocalError)
    case ResultCode.ENCODING_ERROR => Left(AuthenticationError.LDAPEncodingError)
    case ResultCode.DECODING_ERROR => Left(AuthenticationError.LDAPDecodingError)
    case ResultCode.TIMEOUT => Left(AuthenticationError.LDAPTimeout)
    case ResultCode.AUTH_UNKNOWN => Left(AuthenticationError.LDAPAuthUnknown)
    case ResultCode.FILTER_ERROR => Left(AuthenticationError.LDAPFilterError)
    case ResultCode.USER_CANCELED => Left(AuthenticationError.LDAPUserCanceled)
    case ResultCode.PARAM_ERROR => Left(AuthenticationError.LDAPParamError)
    case ResultCode.NO_MEMORY => Left(AuthenticationError.LDAPNoMemory)
    case ResultCode.CONNECT_ERROR => Left(AuthenticationError.LDAPConnectError)
    case ResultCode.NOT_SUPPORTED => Left(AuthenticationError.LDAPNotSupported)
    case ResultCode.CONTROL_NOT_FOUND => Left(AuthenticationError.LDAPControlNotFound)
    case ResultCode.NO_RESULTS_RETURNED => Left(AuthenticationError.LDAPNoResultsReturned)
    case ResultCode.MORE_RESULTS_TO_RETURN => Left(AuthenticationError.LDAPMoreResultsToReturn)
    case ResultCode.CLIENT_LOOP => Left(AuthenticationError.LDAPClientLoop)
    case ResultCode.REFERRAL_LIMIT_EXCEEDED => Left(AuthenticationError.LDAPReferralLimitExceeded)
    case ResultCode.CANCELED => Left(AuthenticationError.LDAPCanceled)
    case ResultCode.NO_SUCH_OPERATION => Left(AuthenticationError.LDAPNoSuchOperation)
    case ResultCode.TOO_LATE => Left(AuthenticationError.LDAPTooLate)
    case ResultCode.CANNOT_CANCEL => Left(AuthenticationError.LDAPCannotCancel)
    case ResultCode.ASSERTION_FAILED => Left(AuthenticationError.LDAPAssertionFailed)
    case ResultCode.AUTHORIZATION_DENIED => Left(AuthenticationError.LDAPAuthorizationDenied)
    case ResultCode.E_SYNC_REFRESH_REQUIRED => Left(AuthenticationError.LDAPESyncRefreshRequired)
    case ResultCode.NO_OPERATION => Left(AuthenticationError.LDAPNoOperation)
    case ResultCode.INTERACTIVE_TRANSACTION_ABORTED => Left(AuthenticationError.LDAPInteractiveTransactionAborted)
    case ResultCode.DATABASE_LOCK_CONFLICT => Left(AuthenticationError.LDAPDatabaseLockConflict)
    case ResultCode.MIRRORED_SUBTREE_DIGEST_MISMATCH => Left(AuthenticationError.LDAPMirroredSubtreeDigestMismatch)
    case ResultCode.TOKEN_DELIVERY_MECHANISM_UNAVAILABLE => Left(AuthenticationError.LDAPTokenDeliveryMechanismUnavailable)
    case ResultCode.TOKEN_DELIVERY_ATTEMPT_FAILED => Left(AuthenticationError.LDAPTokenDeliveryAttemptFailed)
    case ResultCode.TOKEN_DELIVERY_INVALID_RECIPIENT_ID => Left(AuthenticationError.LDAPTokenDeliveryInvalidRecipientId)
    case ResultCode.TOKEN_DELIVERY_INVALID_ACCOUNT_STATE => Left(AuthenticationError.LDAPTokenDeliveryInvalidAccountState)
    case _ => Left(AuthenticationError.LDAPGenericError)
  }
}
