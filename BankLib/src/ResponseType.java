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
    // used only by client
    IllegalServerResponse
}
