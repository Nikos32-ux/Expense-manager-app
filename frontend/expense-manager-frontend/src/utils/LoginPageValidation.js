
 export const loginFieldsValidation = (email, password, setIsMissingField) => {
    if(!email.trim() || !password.trim()){
       setIsMissingField(true);
       setTimeout(() => {
        setIsMissingField(false)
       }, 2000);
       return false;
    }

    return true;
}

