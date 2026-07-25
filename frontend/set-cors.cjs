const { Storage } = require('@google-cloud/storage');
const path = require('path');

// Initialize storage with the backend's service account key
const storage = new Storage({
  projectId: 'smart-ledger-a1775',
  keyFilename: path.join(__dirname, '../backend/src/main/resources/serviceAccountKey.json'),
});

// Using the correct bucket name from .env
const bucketName = 'smart-ledger-a1775.firebasestorage.app';

async function configureBucketCors() {
  const corsConfiguration = [
    {
      maxAgeSeconds: 3600,
      method: ['GET', 'PUT', 'POST', 'DELETE', 'OPTIONS'],
      origin: ['*'],
      responseHeader: ['Content-Type', 'Authorization', 'Content-Length', 'User-Agent', 'x-goog-resumable'],
    },
  ];

  try {
    await storage.bucket(bucketName).setCorsConfiguration(corsConfiguration);
    console.log(`Bucket ${bucketName} was updated with a CORS config to allow requests from any origin.`);
  } catch (error) {
    console.error('Error updating CORS configuration:', error);
  }
}

configureBucketCors();
