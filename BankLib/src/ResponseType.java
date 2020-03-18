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
    // used only by client
    IllegalServerResponse
}
