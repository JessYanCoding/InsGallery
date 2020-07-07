package com.luck.picture.lib.instagram.process;

import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;


public class VideoClipUtils {
    private static final String TAG = VideoClipUtils.class.getSimpleName();
    private static final int DEFAULT_BUFFER_SIZE = 1 * 1024 * 1024;


    /**
     * @param srcPath the path of source video file.
     * @param dstPath the path of destination video file.
     * @param startMs starting time in milliseconds for trimming. Set to
     *            negative if starting from beginning.
     * @param endMs end time for trimming in milliseconds. Set to negative if
     *            no trimming at the end.
     * @param useAudio true if keep the audio track from the source.
     * @param useVideo true if keep the video track from the source.
     * @throws IOException
     */
    public static boolean genVideoUsingMuxer(FileDescriptor srcPath, String dstPath,
                                           long startMs, long endMs, boolean useAudio, boolean useVideo)
            throws IOException {
        // Set up MediaExtractor to read from the source.
        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(srcPath);
        int trackCount = extractor.getTrackCount();
        // Set up MediaMuxer for the destination.
        MediaMuxer muxer;
        muxer = new MediaMuxer(dstPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        // Set up the tracks and retrieve the max buffer size for selected
        // tracks.
        HashMap<Integer, Integer> indexMap = new HashMap<Integer,
                Integer>(trackCount);
        int bufferSize = -1;
        try {
            for (int i = 0; i < trackCount; i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                boolean selectCurrentTrack = false;
                if (mime.startsWith("audio/") && useAudio) {
                    selectCurrentTrack = true;
                } else if (mime.startsWith("video/") && useVideo) {
                    selectCurrentTrack = true;
                }
                if (selectCurrentTrack) {
                    extractor.selectTrack(i);
                    int dstIndex = muxer.addTrack(format);
                    indexMap.put(i, dstIndex);
                    if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                        int newSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                        bufferSize = newSize > bufferSize ? newSize : bufferSize;
                    }
                }
            }
            if (bufferSize < 0) {
                bufferSize = DEFAULT_BUFFER_SIZE;
            }
            // Set up the orientation and starting time for extractor.
            MediaMetadataRetriever retrieverSrc = new MediaMetadataRetriever();
            retrieverSrc.setDataSource(srcPath);
            String degreesString = retrieverSrc.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            if (degreesString != null) {
                int degrees = Integer.parseInt(degreesString);
                if (degrees >= 0) {
                    muxer.setOrientationHint(degrees);
                }
            }
            if (startMs > 0) {
                extractor.seekTo(startMs * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            }
            // Copy the samples from MediaExtractor to MediaMuxer. We will loop
            // for copying each sample and stop when we get to the end of the source
            // file or exceed the end time of the trimming.
            int offset = 0;
            int trackIndex = -1;
            ByteBuffer dstBuf = ByteBuffer.allocate(bufferSize);
            BufferInfo bufferInfo = new BufferInfo();
            muxer.start();
            while (true) {
                bufferInfo.offset = offset;
                bufferInfo.size = extractor.readSampleData(dstBuf, offset);
                if (bufferInfo.size < 0) {
                    Log.d(TAG, "Saw input EOS.");
                    bufferInfo.size = 0;
                    break;
                } else {
                    bufferInfo.presentationTimeUs = extractor.getSampleTime();
                    if (endMs > 0 && bufferInfo.presentationTimeUs > (endMs * 1000)) {
                        Log.d(TAG, "The current sample is over the trim end time.");
                        break;
                    } else {
                        bufferInfo.flags = extractor.getSampleFlags();
                        trackIndex = extractor.getSampleTrackIndex();
                        muxer.writeSampleData(indexMap.get(trackIndex), dstBuf,
                                bufferInfo);
                        extractor.advance();
                    }
                }
            }
            muxer.stop();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            muxer.release();
        }
        return false;
    }

//    /**
//     * 裁剪视频
//     * @param srcPath 需要裁剪的原视频路径
//     * @param outPath 裁剪后的视频输出路径
//     * @param startTimeMs 裁剪的起始时间
//     * @param endTimeMs 裁剪的结束时间
//     */
//    public static boolean trimUsingMp4Parser(FileDescriptor srcPath, String outPath, double startTimeMs, double endTimeMs) throws IOException, IllegalArgumentException {
//        if (srcPath == null || TextUtils.isEmpty(outPath)) {
//            return false;
//        }
//        if (startTimeMs >= endTimeMs) {
//            return false;
//        }
//        Movie movie = MovieCreator.build(new FileDataSourceViaHeapImpl(new FileInputStream(srcPath).getChannel()));
//        List<Track> tracks = movie.getTracks();
//        //移除旧的track
//        movie.setTracks(new LinkedList<Track>());
//        //处理的时间以秒为单位
//        double startTime = startTimeMs/1000;
//        double endTime = endTimeMs/1000;
//
//        boolean timeCorrected = false;
//        // Here we try to find a track that has sync samples. Since we can only
//        // start decoding at such a sample we SHOULD make sure that the start of
//        // the new fragment is exactly such a frame.
//        for (Track track : tracks) {
//            if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
//                if (timeCorrected) {
//                    // This exception here could be a false positive in case we
//                    // have multiple tracks with sync samples at exactly the
//                    // same positions. E.g. a single movie containing multiple
//                    // qualities of the same video (Microsoft Smooth Streaming
//                    // file)
//                    return false;
//                }
//                startTime = correctTimeToSyncSample(track, startTime, false);
//                endTime = correctTimeToSyncSample(track, endTime, true);
//                timeCorrected = true;
//            }
//        }
//
//        long currentSample;
//        double currentTime;
//        double lastTime;
//        long startSample1;
//        long endSample1;
//        long delta;
//
//        for (Track track : tracks) {
//            currentSample = 0;
//            currentTime = 0;
//            lastTime = -1;
//            startSample1 = -1;
//            endSample1 = -1;
//
//            //根据起始时间和截止时间获取起始sample和截止sample的位置
//            for (int i = 0; i < track.getSampleDurations().length; i++) {
//                delta = track.getSampleDurations()[i];
//                if (currentTime > lastTime && currentTime <= startTime) {
//                    startSample1 = currentSample;
//                }
//                if (currentTime > lastTime && currentTime <= endTime) {
//                    endSample1 = currentSample;
//                }
//                lastTime = currentTime;
//                currentTime += (double)delta / (double)track.getTrackMetaData().getTimescale();
//                currentSample++;
//            }
//            if (startSample1 <= 0 && endSample1 <= 0) {
//                throw new RuntimeException("clip failed !!");
//            }
//            movie.addTrack(new CroppedTrack(track, startSample1, endSample1));// 添加截取的track
//        }
//
//        //合成视频mp4
//        Container out = new DefaultMp4Builder().build(movie);
//        FileOutputStream fos = new FileOutputStream(outPath);
//        FileChannel fco = fos.getChannel();
//        out.writeContainer(fco);
//        fco.close();
//        fos.close();
//        return true;
//    }


//    /**
//     * 换算剪切时间
//     * @param track
//     * @param cutHere
//     * @param next
//     * @return
//     */
//    public static double correctTimeToSyncSample(Track track, double cutHere, boolean next) {
//        double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
//        long currentSample = 0;
//        double currentTime = 0;
//        for (int i = 0; i < track.getSampleDurations().length; i++) {
//            long delta = track.getSampleDurations()[i];
//            int index = Arrays.binarySearch(track.getSyncSamples(), currentSample + 1);
//            if (index >= 0) {
//                timeOfSyncSamples[index] = currentTime;
//            }
//            currentTime += ((double)delta / (double)track.getTrackMetaData().getTimescale());
//            currentSample++;
//        }
//        double previous = 0;
//        for (double timeOfSyncSample : timeOfSyncSamples) {
//            if (timeOfSyncSample > cutHere) {
//                if (next) {
//                    return timeOfSyncSample;
//                } else {
//                    return previous;
//                }
//            }
//            previous = timeOfSyncSample;
//        }
//        return timeOfSyncSamples[timeOfSyncSamples.length - 1];
//    }

}
