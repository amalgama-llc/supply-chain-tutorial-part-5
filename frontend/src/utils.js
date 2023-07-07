

export const formatDateTime = (t) => {
  if (t) {
    return t.replace("T", " ");
  }
  return "";
};


export const numberWithSpaces = (number) => {
  let parts = number.toString().split(".");
  parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, " ");
  return parts.join(".");
};
