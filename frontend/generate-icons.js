const fs = require('fs');
const path = require('path');

// Simple function to create SVG
function createSVG(size) {
  return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${size} ${size}" width="${size}" height="${size}">
  <rect width="${size}" height="${size}" fill="#1976d2"/>
  <text x="50%" y="50%" font-family="Arial, sans-serif" font-size="${size * 0.4}" fill="white" text-anchor="middle" dominant-baseline="middle" font-weight="bold">EV</text>
</svg>`;
}

// Create SVG files
const publicDir = path.join(__dirname, 'public');

// Create logo192.png as SVG temporarily
fs.writeFileSync(path.join(publicDir, 'logo.svg'), createSVG(512));

console.log('Created logo.svg - you can convert this to PNG and ICO formats using an online tool');
console.log('Or use: npx sharp-cli resize 192 192 logo.svg -o logo192.png');
console.log('Or use: npx sharp-cli resize 512 512 logo.svg -o logo512.png');
