Video File Upload in Chunks
This project demonstrates how to upload large video files in chunks to a server. Chunked file uploading is especially useful for handling large files in environments with unstable network connections or file size limitations.

Features
Chunked Upload: Breaks large video files into smaller chunks and uploads them individually.
Resumable Upload: Supports resuming uploads if the process is interrupted.
Progress Tracking: Tracks the upload progress of each chunk and provides real-time updates.
Support for Android 12+: The implementation is compatible with Android 12 and above.
Prerequisites
Android Studio with a minimum SDK level of 21 (Android 5.0, Lollipop).
For Android 12 and above, ensure proper permissions for managing large file transfers.
How It Works
Chunk Splitting: The video file is divided into smaller parts (chunks) of a predefined size.
Upload Sequence: Each chunk is uploaded sequentially to the server, and the server confirms receipt.
Error Handling: If an upload fails, only the failed chunk is retried.
Resumption: In the event of network failure or an app restart, the upload resumes from the last successfully uploaded chunk.
Installation

Open the project in Android Studio.

Sync the project to download dependencies.

Setup
Step 1: Permissions
Make sure the following permissions are declared in your AndroidManifest.xml:

xml
Copy code
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
For Android 12+, request specific permissions for media file access:

xml
Copy code
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />
Step 2: Implement File Chunking
The video file is split into chunks using a defined chunk size (e.g., 1 MB per chunk). Example code:

kotlin
Copy code
fun splitFileIntoChunks(file: File, chunkSize: Int): List<File> {
    val chunks = mutableListOf<File>()
    // Logic to divide the file into chunks and store them temporarily
    return chunks
}

Step 3: Upload Each Chunk
Each chunk is uploaded in sequence, and a response from the server confirms success:

kotlin
Copy code
fun uploadChunk(chunk: File, serverUrl: String): Boolean {

    // Logic to upload a chunk of the video file
    // Return true if successful
    return true
}

Step 4: Resume Interrupted Uploads
The system saves the index of the last successfully uploaded chunk. If an upload is interrupted, the process resumes from the last chunk:

kotlin
Copy code

fun resumeUpload(startChunkIndex: Int, chunks: List<File>, serverUrl: String) {

    for (i in startChunkIndex until chunks.size) {
        // Upload the remaining chunks
    }
}
Usage
Select a video file from storage.
The file will automatically be split into chunks.
Upload will begin, and progress can be tracked.
If interrupted, the upload can resume from where it left off.
Example
kotlin
Copy code

val videoFile = File("/path/to/video.mp4")
val chunkSize = 1024 * 1024  // 1 MB
val chunks = splitFileIntoChunks(videoFile, chunkSize)


chunks.forEachIndexed { index, chunk ->

    val success = uploadChunk(chunk, "https://your-server/upload")
    if (!success) {
    
        // Retry logic
    }
}

Notes
Ensure the server-side implementation supports chunked uploads and properly handles the reassembly of the file.
Make sure the file size and chunk size are appropriate for your use case.
