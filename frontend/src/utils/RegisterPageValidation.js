
export const registerFieldsValidation = (username, password, email, imageProfile, setMissingField, setIsError, setErrorFieldMessage) => {
    if (!username || !password || !email || !imageProfile) {
        setMissingField(true);
        setTimeout(() => {
            setMissingField(false);
        }, 2000)
        return false;
    }

    if (username.length < 3 || username.length > 10) {
        setIsError(true);
        setErrorFieldMessage("Username must be between 3 and 10 characters long");
        setTimeout(() => {
            setIsError(false);
        }, 2000)
        return false;
    };

    if (password.length <= 8 || password.length > 30) {
        setIsError(true);
        setErrorFieldMessage("Password must be between 8 and 30 characters long");
        setTimeout(() => {
            setIsError(false);
        }, 2000)
        return false;
    };

    if (email.length < 5 || email.length > 30) {
        setIsError(true);
        setErrorFieldMessage("Email must be between 5 and 20 characters long");
        setTimeout(() => {
            setIsError(false);
        }, 8000)
        return false;
    };

    return true;
}
