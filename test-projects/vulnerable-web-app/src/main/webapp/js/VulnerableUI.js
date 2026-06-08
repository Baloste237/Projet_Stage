// 4. Cross-Site Scripting (triggers XSS rules)
function injectContent(userInput) {
    // triggers document.write rule
    document.write("<h1>" + userInput + "</h1>");

    // triggers innerHTML rule
    const div = document.getElementById("output");
    div.innerHTML = userInput;
}
