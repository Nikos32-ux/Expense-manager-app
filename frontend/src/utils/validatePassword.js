 export const checkPassRules = (pass) => {
    return [
      { id: 1, type: "length", rule: "8 to 25 characters", valid: pass.length >= 8 && pass.length <= 25 },
      { id: 2, type: "uppercase", rule: "An uppercase letter", valid: /[A-Z]/.test(pass) },
      { id: 3, type: "lowercase", rule: "A lowercase letter", valid: /[a-z]/.test(pass) },
      { id: 4, type: "special", rule: "A special character", valid: /[@$!%*?&]/.test(pass) },
      { id: 5, type: "number", rule: "A number", valid: /\d/.test(pass) }
    ]
  }