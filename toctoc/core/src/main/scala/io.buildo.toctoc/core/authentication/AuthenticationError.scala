package io.buildo.toctoc
package core
package authentication

sealed trait AuthenticationError
object AuthenticationError {
  // toctoc error
  object InvalidCredential extends AuthenticationError
  object Forbidden extends AuthenticationError
  object CredentialInsertError extends AuthenticationError
  // LDAP errors
  object LDAPOperationsError extends AuthenticationError
  object LDAPProtocolError extends AuthenticationError
  object LDAPTimeLimitExceeded extends AuthenticationError
  object LDAPSizeLimitExceeded extends AuthenticationError
  object LDAPCompareFalse extends AuthenticationError
  object LDAPCompareTrue extends AuthenticationError
  object LDAPAuthMethodNotSupported extends AuthenticationError
  object LDAPStrongAuthRequired extends AuthenticationError
  object LDAPReferral extends AuthenticationError
  object LDAPAdminLimitExceeded extends AuthenticationError
  object LDAPUnavailableCriticalExtension extends AuthenticationError
  object LDAPConfidentialityRequired extends AuthenticationError
  object LDAPSaslBindInProgress extends AuthenticationError
  object LDAPNoSuchAttribute extends AuthenticationError
  object LDAPUndefinedAttributeType extends AuthenticationError
  object LDAPInappropriateMatching extends AuthenticationError
  object LDAPConstraintViolation extends AuthenticationError
  object LDAPAttributeOrValueExists extends AuthenticationError
  object LDAPInvalidAttributeSyntax extends AuthenticationError
  object LDAPNoSuchObject extends AuthenticationError
  object LDAPAliasProblem extends AuthenticationError
  object LDAPInvalidDnSyntax extends AuthenticationError
  object LDAPAliasDereferencingProblem extends AuthenticationError
  object LDAPInappropriateAuthentication extends AuthenticationError
  object LDAPInvalidCredentials extends AuthenticationError
  object LDAPInsufficientAccessRights extends AuthenticationError
  object LDAPBusy extends AuthenticationError
  object LDAPUnavailable extends AuthenticationError
  object LDAPUnwillingToPerform extends AuthenticationError
  object LDAPLoopDetect extends AuthenticationError
  object LDAPSortControlMissing extends AuthenticationError
  object LDAPOffsetRangeError extends AuthenticationError
  object LDAPNamingViolation extends AuthenticationError
  object LDAPObjectClassViolation extends AuthenticationError
  object LDAPNotAllowedOnNonleaf extends AuthenticationError
  object LDAPNotAllowedOnRdn extends AuthenticationError
  object LDAPEntryAlreadyExists extends AuthenticationError
  object LDAPObjectClassModsProhibited extends AuthenticationError
  object LDAPAffectsMultipleDsas extends AuthenticationError
  object LDAPVirtualListViewError extends AuthenticationError
  object LDAPOther extends AuthenticationError
  object LDAPServerDown extends AuthenticationError
  object LDAPLocalError extends AuthenticationError
  object LDAPEncodingError extends AuthenticationError
  object LDAPDecodingError extends AuthenticationError
  object LDAPTimeout extends AuthenticationError
  object LDAPAuthUnknown extends AuthenticationError
  object LDAPFilterError extends AuthenticationError
  object LDAPUserCanceled extends AuthenticationError
  object LDAPParamError extends AuthenticationError
  object LDAPNoMemory extends AuthenticationError
  object LDAPConnectError extends AuthenticationError
  object LDAPNotSupported extends AuthenticationError
  object LDAPControlNotFound extends AuthenticationError
  object LDAPNoResultsReturned extends AuthenticationError
  object LDAPMoreResultsToReturn extends AuthenticationError
  object LDAPClientLoop extends AuthenticationError
  object LDAPReferralLimitExceeded extends AuthenticationError
  object LDAPCanceled extends AuthenticationError
  object LDAPNoSuchOperation extends AuthenticationError
  object LDAPTooLate extends AuthenticationError
  object LDAPCannotCancel extends AuthenticationError
  object LDAPAssertionFailed extends AuthenticationError
  object LDAPAuthorizationDenied extends AuthenticationError
  object LDAPESyncRefreshRequired extends AuthenticationError
  object LDAPNoOperation extends AuthenticationError
  object LDAPInteractiveTransactionAborted extends AuthenticationError
  object LDAPDatabaseLockConflict extends AuthenticationError
  object LDAPMirroredSubtreeDigestMismatch extends AuthenticationError
  object LDAPTokenDeliveryMechanismUnavailable extends AuthenticationError
  object LDAPTokenDeliveryAttemptFailed extends AuthenticationError
  object LDAPTokenDeliveryInvalidRecipientId extends AuthenticationError
  object LDAPTokenDeliveryInvalidAccountState extends AuthenticationError
  // LDAP fallback error
  object LDAPGenericError extends AuthenticationError
}
