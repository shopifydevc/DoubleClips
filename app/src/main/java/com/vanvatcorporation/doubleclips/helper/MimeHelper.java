package com.vanvatcorporation.doubleclips.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class MimeHelper {

    // Enum for common MIME types
    public enum MimeType {
            // Images
            IMAGE_JPEG("image/jpeg", "Joint Photographic Experts Group (JPEG)"),
            IMAGE_PNG("image/png", "Portable Network Graphics (PNG)"),
            IMAGE_GIF("image/gif", "Graphics Interchange Format (GIF)"),
            IMAGE_BMP("image/bmp", "Bitmap Image File (BMP)"),
            IMAGE_WEBP("image/webp", "Web Picture Format (WebP)"),
            IMAGE_TIFF("image/tiff", "Tagged Image File Format (TIFF)"),
            IMAGE_SVG("image/svg+xml", "Scalable Vector Graphics (SVG)"),

            // Audio
            AUDIO_MP3("audio/mpeg", "MPEG Audio Layer III (MP3)"),
            AUDIO_WAV("audio/wav", "Waveform Audio File Format (WAV)"),
            AUDIO_OGG("audio/ogg", "Ogg Vorbis Audio (OGG)"),
            AUDIO_FLAC("audio/flac", "Free Lossless Audio Codec (FLAC)"),
            AUDIO_AAC("audio/aac", "Advanced Audio Coding (AAC)"),
            AUDIO_MIDI("audio/midi", "Musical Instrument Digital Interface (MIDI)"),
            AUDIO_WEBM("audio/webm", "WebM Audio"),

            // Video
            VIDEO_MP4("video/mp4", "MPEG-4 Video (MP4)"),
            VIDEO_WEBM("video/webm", "WebM Video"),
            VIDEO_OGG("video/ogg", "Ogg Theora Video (OGG)"),
            VIDEO_AVI("video/x-msvideo", "Audio Video Interleave (AVI)"),
            VIDEO_MOV("video/quicktime", "QuickTime Movie (MOV)"),
            VIDEO_MPEG("video/mpeg", "Moving Picture Experts Group (MPEG)"),
            VIDEO_3GP("video/3gpp", "3rd Generation Partnership Project (3GP)"),
            VIDEO_MKV("video/x-matroska", "Matroska Video (MKV)"),

            // Documents
            TEXT_PLAIN("text/plain", "Plain Text File"),
            APPLICATION_PDF("application/pdf", "Portable Document Format (PDF)"),
            APPLICATION_JSON("application/json", "JavaScript Object Notation (JSON)"),

            // Fallback
            UNKNOWN("application/octet-stream", "Unknown / Binary Stream");

            private final String type;
            private final String fullName;

            MimeType(String type, String fullName) {
                this.type = type;
                this.fullName = fullName;
            }

            public String getType() {
                return type;
            }

            public String getFullName() {
                return fullName;
            }





        public boolean isVideo() {
            return type.startsWith("video/");
        }

        public boolean isAudio() {
            return type.startsWith("audio/");
        }

        public boolean isImage() {
            return type.startsWith("image/");
        }


        // Reverse lookup map for quick access
        private static final Map<String, MimeType> LOOKUP = new HashMap<>();
        static {
            for (MimeType mt : values()) {
                LOOKUP.put(mt.type, mt);
            }
        }

        public static MimeType fromString(String mime) {
            if (mime == null) return UNKNOWN;
            MimeType mt = LOOKUP.get(mime.toLowerCase());
            return mt != null ? mt : UNKNOWN;
        }
    }


    /**
     * Get MIME type from a Uri and return as MimeType enum.
     */
    @NonNull
    public static MimeType getMimeTypeFromUri(@NonNull Context context, @NonNull Uri uri) {
        String mimeType = null;

        // Try to get from ContentResolver
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            mimeType = context.getContentResolver().getType(uri);
        } else {
            // Get from file extension
            String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            if (extension != null) {
                mimeType = MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(extension.toLowerCase());
            }
        }

        return MimeType.fromString(mimeType);
    }

    /**
     * Get MIME type from a file path and return as MimeType enum.
     */
    @NonNull
    public static MimeType getMimeTypeFromPath(@NonNull String filePath) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
        String mimeType = null;
        if (extension != null) {
            mimeType = MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(extension.toLowerCase());
        }
        return MimeType.fromString(mimeType);
    }

}


