/**
 * Used for recognizing type of response sent from server to client
 */
public enum ResponseType {
    Success,
    EmailAlreadySignedUp,
    AccountInfo,
    InternalError,
    ArgumentMissingError,
    NotLoggedInError,
    IncorrectLoginError,
    // used only by server
    IllegalRequestResponse,
    AccountCreateFailResponse,
    InvalidReceiverIDResponse,
    UnknownErrorResponse,
    SuccessPaymentResponse,
    PaymentHistoryResponse,
    PaymentWithDelayResponse,
    // used only by client
    IllegalServerResponse
}
