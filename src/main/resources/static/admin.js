function showNotification(text, bg, color, border) {
    const banner = document.getElementById('statusMessage');
    banner.textContent = text;
    banner.style.backgroundColor = bg;
    banner.style.color = color;
    banner.style.border = `1px solid ${border}`;
    banner.style.display = 'block';
}