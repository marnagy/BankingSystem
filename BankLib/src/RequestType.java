/**
 * Used for recognizing type of request sent from client to server
 */
public enum RequestType {
    CreateAccount,
    Login,
    Payment,
    PaymentWithDelay,
    PaymentCategoryChange,
    AccountHistory,
    Logout,
    End
}
