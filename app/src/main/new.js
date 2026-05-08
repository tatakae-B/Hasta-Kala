service cloud.firestore {
  match /databases/{database}/documents {
    // Matches any document in the 'users' collection or its subcollections
    match /users/{userId}/{document=**} {
      // Allows access only if the logged-in user's UID matches the folder ID
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}