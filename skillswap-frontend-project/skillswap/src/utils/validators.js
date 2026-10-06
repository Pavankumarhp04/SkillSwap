export function isRequired(value) {
  return value !== null && value !== undefined && String(value).trim() !== "";
}

export function isValidEmail(email) {
  if (!email) {
    return false;
  }

  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim());
}

export function isValidPassword(password) {
  return typeof password === "string" && password.length >= 6;
}

export function passwordsMatch(password, confirmPassword) {
  return password === confirmPassword;
}

export function isValidName(name) {
  if (!name) {
    return false;
  }

  return name.trim().length >= 2 && /^[A-Za-z ]+$/.test(name.trim());
}

export function isValidRating(rating) {
  const value = Number(rating);

  return !Number.isNaN(value) && value >= 1 && value <= 5;
}
