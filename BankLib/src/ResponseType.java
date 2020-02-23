public enum ResponseType {
    Success,
    EmailAlreadySignedUp,
    AccountInfo,
    InternalError,
    ArgumentMissingError,
    NotLoggedInError,
    IncorrectLoginError,
    //used only by client
    IllegalResponse
}
