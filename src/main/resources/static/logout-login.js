function executeLogout() {
sessionStorage.removeItem('jwtToken');
sessionStorage.removeItem('role');
sessionStorage.removeItem('username');
window.location.href = 'products.html';
// window.location.reload();
}

function displayUsername() {
    const username = sessionStorage.getItem('username');
    if (token) document.getElementById('userGreeting').textContent = `Hi, ${username}`;
}

function toggleAdminDashboard() {
    const role = sessionStorage.getItem('role');
    if (role === 'ROLE_ADMIN') {
        document.getElementById('adminDashboard_').style.display = "";
        document.getElementById('adminDashboard').style.display = "";
    }
}

function updateAuthLink() {
    const authLink = document.getElementById('authLink');
    const token = sessionStorage.getItem('jwtToken');

    if (token) {
        authLink.textContent = 'Log out';
        authLink.onclick = function(event) {
            event.preventDefault();
            executeLogout();
        };
    } else {
        authLink.textContent = 'Login';
        authLink.onclick = function(event) {
            event.preventDefault();
            window.location.href = 'login.html';
        };
    }
}