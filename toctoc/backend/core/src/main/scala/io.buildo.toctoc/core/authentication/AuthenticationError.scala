package io.buildo.toctoc
package core
package authentication

import io.buildo.enumero.annotations.enum

@enum trait AuthenticationError {
  // toctoc error
  object InvalidCredential
  object Forbidden
  // LDAP errors
  object LDAPOperationsError
  object LDAPProtocolError
  object LDAPTimeLimitExceeded
  object LDAPSizeLimitExceeded
  object LDAPCompareFalse
  object LDAPCompareTrue
  object LDAPAuthMethodNotSupported
  object LDAPStrongAuthRequired
  object LDAPReferral
  object LDAPAdminLimitExceeded
  object LDAPUnavailableCriticalExtension
  object LDAPConfidentialityRequired
  object LDAPSaslBindInProgress
  object LDAPNoSuchAttribute
  object LDAPUndefinedAttributeType
  object LDAPInappropriateMatching
  object LDAPConstraintViolation
  object LDAPAttributeOrValueExists
  object LDAPInvalidAttributeSyntax
  object LDAPNoSuchObject
  object LDAPAliasProblem
  object LDAPInvalidDnSyntax
  object LDAPAliasDereferencingProblem
  object LDAPInappropriateAuthentication
  object LDAPInvalidCredentials
  object LDAPInsufficientAccessRights
  object LDAPBusy
  object LDAPUnavailable
  object LDAPUnwillingToPerform
  object LDAPLoopDetect
  object LDAPSortControlMissing
  object LDAPOffsetRangeError
  object LDAPNamingViolation
  object LDAPObjectClassViolation
  object LDAPNotAllowedOnNonleaf
  object LDAPNotAllowedOnRdn
  object LDAPEntryAlreadyExists
  object LDAPObjectClassModsProhibited
  object LDAPAffectsMultipleDsas
  object LDAPVirtualListViewError
  object LDAPOther
  object LDAPServerDown
  object LDAPLocalError
  object LDAPEncodingError
  object LDAPDecodingError
  object LDAPTimeout
  object LDAPAuthUnknown
  object LDAPFilterError
  object LDAPUserCanceled
  object LDAPParamError
  object LDAPNoMemory
  object LDAPConnectError
  object LDAPNotSupported
  object LDAPControlNotFound
  object LDAPNoResultsReturned
  object LDAPMoreResultsToReturn
  object LDAPClientLoop
  object LDAPReferralLimitExceeded
  object LDAPCanceled
  object LDAPNoSuchOperation
  object LDAPTooLate
  object LDAPCannotCancel
  object LDAPAssertionFailed
  object LDAPAuthorizationDenied
  object LDAPESyncRefreshRequired
  object LDAPNoOperation
  object LDAPInteractiveTransactionAborted
  object LDAPDatabaseLockConflict
  object LDAPMirroredSubtreeDigestMismatch
  object LDAPTokenDeliveryMechanismUnavailable
  object LDAPTokenDeliveryAttemptFailed
  object LDAPTokenDeliveryInvalidRecipientId
  object LDAPTokenDeliveryInvalidAccountState
  // LDAP fallback error
  object LDAPGenericError
}
