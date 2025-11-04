const fs = require('fs');
const path = require('path');

// Simple 32x32 blue square PNG (base64 encoded)
// This is a minimal valid PNG file
const createPNG = (size, color = [25, 118, 210]) => {
  const PNG_SIGNATURE = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]);

  // Create IHDR chunk
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(size, 0); // width
  ihdr.writeUInt32BE(size, 4); // height
  ihdr.writeUInt8(8, 8); // bit depth
  ihdr.writeUInt8(2, 9); // color type (RGB)
  ihdr.writeUInt8(0, 10); // compression
  ihdr.writeUInt8(0, 11); // filter
  ihdr.writeUInt8(0, 12); // interlace

  // Create image data
  const bytesPerPixel = 3;
  const bytesPerRow = 1 + size * bytesPerPixel; // 1 byte filter + pixels
  const imageData = Buffer.alloc(size * bytesPerRow);

  for (let y = 0; y < size; y++) {
    imageData.writeUInt8(0, y * bytesPerRow); // filter type
    for (let x = 0; x < size; x++) {
      const pixelOffset = y * bytesPerRow + 1 + x * bytesPerPixel;
      imageData.writeUInt8(color[0], pixelOffset);     // R
      imageData.writeUInt8(color[1], pixelOffset + 1); // G
      imageData.writeUInt8(color[2], pixelOffset + 2); // B
    }
  }

  // Compress using Node's zlib
  const zlib = require('zlib');
  const compressedData = zlib.deflateSync(imageData);

  // Helper to create chunk
  const createChunk = (type, data) => {
    const length = Buffer.alloc(4);
    length.writeUInt32BE(data.length, 0);

    const typeBuffer = Buffer.from(type, 'ascii');
    const crcBuffer = Buffer.concat([typeBuffer, data]);

    // Simple CRC32 calculation
    const crc32 = require('zlib').crc32 || ((buf) => {
      let crc = 0xFFFFFFFF;
      for (let i = 0; i < buf.length; i++) {
        crc ^= buf[i];
        for (let j = 0; j < 8; j++) {
          crc = (crc >>> 1) ^ (0xEDB88320 & -(crc & 1));
        }
      }
      return (crc ^ 0xFFFFFFFF) >>> 0;
    });

    const crc = Buffer.alloc(4);
    crc.writeUInt32BE(crc32(crcBuffer), 0);

    return Buffer.concat([length, typeBuffer, data, crc]);
  };

  const ihdrChunk = createChunk('IHDR', ihdr);
  const idatChunk = createChunk('IDAT', compressedData);
  const iendChunk = createChunk('IEND', Buffer.alloc(0));

  return Buffer.concat([PNG_SIGNATURE, ihdrChunk, idatChunk, iendChunk]);
};

// Simple ICO file (contains a 16x16 image)
const createICO = () => {
  // For simplicity, create a minimal ICO with embedded PNG
  const png16 = createPNG(16);

  const header = Buffer.alloc(6);
  header.writeUInt16LE(0, 0); // Reserved
  header.writeUInt16LE(1, 2); // Type (1 = ICO)
  header.writeUInt16LE(1, 4); // Number of images

  const dirEntry = Buffer.alloc(16);
  dirEntry.writeUInt8(16, 0);  // Width
  dirEntry.writeUInt8(16, 1);  // Height
  dirEntry.writeUInt8(0, 2);   // Color palette
  dirEntry.writeUInt8(0, 3);   // Reserved
  dirEntry.writeUInt16LE(1, 4); // Color planes
  dirEntry.writeUInt16LE(32, 6); // Bits per pixel
  dirEntry.writeUInt32LE(png16.length, 8); // Image size
  dirEntry.writeUInt32LE(22, 12); // Image offset

  return Buffer.concat([header, dirEntry, png16]);
};

const publicDir = path.join(__dirname, 'public');

try {
  // Create favicon.ico (16x16)
  const favicon = createICO();
  fs.writeFileSync(path.join(publicDir, 'favicon.ico'), favicon);
  console.log('✓ Created favicon.ico');

  // Create logo192.png
  const logo192 = createPNG(192);
  fs.writeFileSync(path.join(publicDir, 'logo192.png'), logo192);
  console.log('✓ Created logo192.png');

  // Create logo512.png
  const logo512 = createPNG(512);
  fs.writeFileSync(path.join(publicDir, 'logo512.png'), logo512);
  console.log('✓ Created logo512.png');

  console.log('\nAll icon files created successfully!');
} catch (error) {
  console.error('Error creating icons:', error);
  process.exit(1);
}
