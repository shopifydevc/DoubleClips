// TODO: CapCut transition in the way that in a 2s transition, it take 0.6s of the end of clipA and 1.4s of the start of clipB, if the clip is cut from the end
//  in our case it's endClipTrim, then it extend to the 0.6s to match the lost clip from transition merging. If it does not have enough endClipTrim,
//  e.g. 0.3s trim only from clipA then it just freeze the frame fro another 0.3s, if it does not have any endClipTrim at all, then the entire 0.6s
//  will be the last frame of clipA, entirely
//  Audio will be merged too
//  .
//  We uses ffmpeg so it should be 1s equally for both clip. So in order to do it, we will need to get the transition duration before the FXCommandEmmiter.java.
//  which then add the "tpad=stop_mode=clone:stop_duration=n" with n is the half of transition duration to the clipA before transition. No need to set "apad=pad_dur=2"
//  for audio-filter-complex because it will not output anything once the sound run out
//  .
//  .
//  .
//  AI Link: https://copilot.microsoft.com/shares/nw39hkpxpiAxGq55Hy5xa



// TODO: Rewritten by AI:






// TODO: Implement a CapCut-style transition where, in a 2s transition, 0.6s comes from the end of clipA and
//  1.4s from the start of clipB. If the cut is at the end (endClipTrim), extend it by 0.6s to make up for
//  what’s lost during the merge. If endClipTrim is less than 0.6s (e.g., 0.3s), freeze the last frame for
//  the remaining 0.3s. If there’s no endClipTrim, use the last frame of clipA for the full 0.6s.
//  Audio should be merged as well.
//  .
//  Using ffmpeg, the transition should be 1s from each clip. To achieve this, get the transition
//  duration before FXCommandEmitter.java, then add "tpad=stop_mode=clone:stop_duration=n" to clipA
//  before the transition, where n is half the transition duration. No need to use "apad=pad_dur=2"
//  for audio-filter-complex since it won’t output anything once the sound ends.


package com.vanvatcorporation.doubleclips;

import android.content.Context;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;
import com.arthenica.ffmpegkit.Statistics;
import com.vanvatcorporation.doubleclips.activities.EditingActivity;
import com.vanvatcorporation.doubleclips.activities.MainActivity;
import com.vanvatcorporation.doubleclips.constants.Constants;
import com.vanvatcorporation.doubleclips.helper.IOHelper;
import com.vanvatcorporation.doubleclips.impl.java.RunnableImpl;
import com.vanvatcorporation.doubleclips.manager.LoggingManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

public class FFmpegEdit {
    public static FfmpegRenderQueue queue = new FfmpegRenderQueue();
    public static void runAnyCommand(Context context, String cmd, String taskName) {
        runAnyCommand(context, cmd, taskName, "Ran command!", "Command failed: ", true);
    }

    public static void runAnyCommand(Context context, String cmd, String taskName,
                                     Runnable onSuccessRunnable, Runnable onFailRunnable,
                                     RunnableImpl onLogRunnable, RunnableImpl onStatisticsRunnable) {
        runAnyCommand(context, cmd, taskName, "Ran command!", "Command failed: ", true, onSuccessRunnable, onFailRunnable, onLogRunnable, onStatisticsRunnable);
    }

    public static void runAnyCommand(Context context, String cmd, String taskName, String successMessage, String failMessage, boolean includeFullReport) {
        runAnyCommand(context, cmd, taskName, successMessage, failMessage, includeFullReport, () -> {
        }, () -> {
        }, new RunnableImpl() {
            @Override
            public <T> void runWithParam(T param) {

            }
        }, new RunnableImpl() {
            @Override
            public <T> void runWithParam(T param) {

            }
        });
    }

    public static void runAnyCommand(Context context, String cmd, String taskName, String successMessage, String failMessage, boolean includeFullReport,
                                     Runnable onSuccessRunnable, Runnable onFailRunnable,
                                     RunnableImpl onLogRunnable, RunnableImpl onStatisticsRunnable) {
        LoggingManager.LogToPersistentDataPath(context, cmd);



        queue.enqueue(
                new FfmpegRenderQueue.FfmpegRenderQueueInfo(
                        taskName,
                        () -> {

                            FFmpegKit.executeAsync(cmd, session -> {
                                        if (ReturnCode.isSuccess(session.getReturnCode())) {
                                            StringBuilder builder = new StringBuilder();
                                            if (includeFullReport) {
                                                builder.append("Report: ").append("\n")
                                                        .append("Output: ").append(session.getOutput()).append("\n")
                                                        .append("State: ").append(session.getState()).append("\n")
                                                        .append("Return code: ").append(session.getReturnCode()).append("\n");
                                            }
                                            LoggingManager.LogToPersistentDataPath(context, successMessage + builder);
                                            onSuccessRunnable.run();
                                        } else {
                                            StringBuilder builder = new StringBuilder();
                                            if (includeFullReport) {
                                                builder.append("Report: ").append("\n")
                                                        .append("Output: ").append(session.getOutput()).append("\n")
                                                        .append("State: ").append(session.getState()).append("\n")
                                                        .append("Return code: ").append(session.getReturnCode()).append("\n")
                                                        .append("Stacktrace: ").append(session.getFailStackTrace()).append("\n");
                                            }
                                            LoggingManager.LogToPersistentDataPath(context, failMessage + builder);
                                            onFailRunnable.run();
                                        }

                                        queue.taskCompleted(); // Move to next task
                                    },
                                    onLogRunnable::runWithParam,
                                    onStatisticsRunnable::runWithParam
                            );

                        }
                )

        );

    }


    public static void generateExportVideo(Context context, EditingActivity.Timeline timeline, EditingActivity.VideoSettings settings, MainActivity.ProjectData data, Runnable onSuccess) {
        runAnyCommand(context, generateExportCmd(context, settings, timeline, data), "Exporting Video", onSuccess, () -> {
        }, new RunnableImpl() {
            @Override
            public <T> void runWithParam(T param) {

            }
        }, new RunnableImpl() {
            @Override
            public <T> void runWithParam(T param) {

            }
        });
    }

    public static String generateExportCmd(Context context, EditingActivity.VideoSettings settings, EditingActivity.Timeline timeline, MainActivity.ProjectData data) {
        FfmpegFilterComplexTags tags = new FfmpegFilterComplexTags();

        StringBuilder cmd = new StringBuilder();
        cmd.append("-f lavfi -i color=c=black:s=")
                .append(settings.getVideoWidth()).append("x").append(settings.getVideoHeight())
                .append(":r=").append(settings.getFrameRate()).append(" -t ").append(timeline.duration).append(" ");

        StringBuilder filterComplex = new StringBuilder();
        StringBuilder audioInputs = new StringBuilder();
        StringBuilder audioMaps = new StringBuilder();

        int inputIndex = 0;
        int audioClipCount = 0;


        int keyframeClipIndex = 0;
        // --- Inserting file path into -i ---
        for (EditingActivity.Track track : timeline.tracks) {
            for (EditingActivity.Clip clip : track.clips) {

                switch (clip.type) {
                    case VIDEO:
                    case IMAGE:
                        cmd.append("-f lavfi -i \"nullsrc=size=")
                                .append(settings.getVideoWidth()).append("x").append(settings.getVideoHeight())
                                .append(":rate=").append(settings.getFrameRate()).append(",format=rgba\"").append(" ");

                        // Since image is a still image, with only one frame. We need to specify it and manipulate it
                        // some how to behave like a video, that way we can use that as a normal video playback
                        // and working with many more effect like transition
                        String frameFilter =
                                clip.type == EditingActivity.ClipType.IMAGE ?
                                        "-loop 1 -t " + clip.duration + " -framerate " + settings.getFrameRate() + " " :
                                        "";

                        cmd.append(frameFilter).append("-i \"").append(clip.getAbsolutePath(data)).append("\" ");
                        break;
                    case AUDIO:
                        cmd.append("-i \"").append(clip.getAbsolutePath(data)).append("\" ");
                        break;
                    case TEXT:
                        cmd.append("-f lavfi -i \"nullsrc=size=")
                                .append(settings.getVideoWidth()).append("x").append(settings.getVideoHeight())
                                .append(":rate=").append(settings.getFrameRate()).append(",format=rgba\"").append(" ");
                        break;

                }
            }
        }


        // --- Inputting clips from -i ---
        String baseTag = "[base]";
        filterComplex.append("[").append(inputIndex).append(":v]trim=duration=").append(timeline.duration).append(",setpts=PTS-STARTPTS").append(baseTag).append(";\n");
        tags.storeTag(baseTag);
        inputIndex++;
        for (EditingActivity.Track track : timeline.tracks) {
            for (EditingActivity.Clip clip : track.clips) {

                String clipLabel = "[video-" + inputIndex + "]";
                String transparentLabel = "[trans-" + inputIndex + "]";
                String outputLabel = "[trans-video-" + inputIndex + "]";


                String audioLabel = "[audio-" + inputIndex + "]";





                // Transition extension
                // First we find the exact clip associated with the TransitionClip's clipA
                // Then extract the half duration, we don't need the rest for now at least
                // This for loop may inefficient, but it works! Optimize this later
                // TODO: Optimize the search.
                float fillingTransitionDuration = 0;

                for (EditingActivity.TransitionClip transition : track.transitions) {
                    if(clip == transition.fromClip && !transition.effect.style.equals("none")) {
                        switch (transition.mode)
                        {
                            case END_FIRST:
                                // 0. End first mean the moment the second clip begin, the fade has completed, so we
                                // doesnt need filling as we begin the transition at the clipA entirely
                                fillingTransitionDuration = 0;
                                break;
                            case OVERLAP:
                                // Duration / 2. Overlap mean half of clipA and half of clipB are join together, we only need
                                // to fill half the clipA as clipB is already get the half.
                                fillingTransitionDuration = transition.duration / 2;
                                break;
                            case BEGIN_SECOND:
                                // Duration. Begin second mean the opposite to end first. The moment the second clip begin,
                                // its when the transition begin, so we need to fill all of the duration that's going to fade
                                fillingTransitionDuration = transition.duration;
                                break;

                        }
                        break;
                    }
                }
                // Because we based on availability of endClipTrim, we first get the few parameters
                // correct adding (extendMediaDuration) and freeze frame duration (freezeFrameDuration)

                // extendMediaDuration: We get the minimum of the clip to extend, if endClipTrim has more than filling
                // then we just take the half duration of transition to extend.
                // if filling is more than the available of clip, which is endClipTrim, then we only extend to the maximum duration of the clip,
                // that mean endClipTrim is meaningless.
                float extendMediaDuration = Math.min(clip.endClipTrim, fillingTransitionDuration);

                // freezeFrameDuration: We get the max value of these 2 variable ( fillingTransitionDuration - clip.endClipTrim and 0 )
                // fillingTransitionDuration - clip.endClipTrim will get the remaining duration after the clip extend all of it endClipTrim
                // Why 0? If the subtraction is negative then it has no freeze frame because there is still enough endClipTrim to extend.
                float freezeFrameDuration = Math.max(fillingTransitionDuration - clip.endClipTrim, 0);



                switch (clip.type) {
                    case VIDEO:
                    case IMAGE:

                        // 🖼️ Video/Image visual logic
                        // Transition extension: Add half of the duration to the transparent layer, if transition isn't exist, then add 0
                        filterComplex.append("[").append(inputIndex).append(":v]")
                                .append("trim=duration=").append(clip.duration + fillingTransitionDuration).append(",")
                                .append("setpts=PTS-STARTPTS+").append(clip.startTime).append("/TB").append(transparentLabel).append(";\n");
                        inputIndex++;

                        // Video can use start and end trim, but image cant, so we need to specify the trim for each type.
                        // Transition extension: This time we don't use raw fillingTransitionDuration like the transparent, but we use the
                        // value we calculate earlier using endClipTrim. Because endClipTrim has already applied to duration, so now we
                        // can just add to it.
                        // Image is just like transparent layer, so we add the raw fillingTransitionDuration
                        String trimFilter =
                                clip.type == EditingActivity.ClipType.VIDEO ?
                                        "trim=start=" + clip.startClipTrim + ":end=" + (clip.startClipTrim + clip.duration + extendMediaDuration) :
                                        "trim=duration=" + (clip.duration + fillingTransitionDuration);

                        // FFmpeg uses radians rotation, so...
                        double radiansRotation = Math.toRadians(clip.rotation);


                        // First we declared the stream of video
                        filterComplex.append("[").append(inputIndex).append(":v]");


                        // Let simulating 4 keyframe type in opacity for example:
                        // K #1: 1 at 1s
                        // K #2: 0 at 2s
                        // K #3: 0 at 3s
                        // K #4: 1 at 4s
                        // Kinda like --\_/--   (\ and _ and / are actually 3 lines created from 4 points)

                        // gte(t,5)*lte(t,10)
                        //colorchannelmixer=aa='if(gte(t,1)*lte(t,2), exp(-0.5*(t-3)), if(gte(t,2)*lte(t,3), 0, if(gte(t,3)*lte(t,4)), 1-exp(-1*t), 1))'



                        // Detect keyframe after which we write our expr compilation
                        // In this first if expr: We process scaleX, scaleY, rot, opacity, speed
                        if (clip.hasAnimatedProperties()) {


                            // TODO: Scale is not applied yet. Research zoompan instead.
                            String scaleXExpr = getKeyframeFFmpegExpr(clip.keyframes.keyframes, 0, EditingActivity.VideoProperties.ValueType.ScaleX);
                            String scaleYExpr = getKeyframeFFmpegExpr(clip.keyframes.keyframes, 0, EditingActivity.VideoProperties.ValueType.ScaleY);

                            String rotationExpr = getKeyframeFFmpegExpr(clip.keyframes.keyframes, 0, EditingActivity.VideoProperties.ValueType.Rot);

                            filterComplex.append("scale=iw*").append(clip.scaleX).append(":ih*").append(clip.scaleY).append(",")
                                    //.append("scale=").append(clip.width).append(":").append(clip.height).append(",")
                                    .append("rotate='").append(rotationExpr).append("':ow=rotw('").append(rotationExpr).append("'):oh=roth('").append(rotationExpr).append("')")
                                    .append(":fillcolor=0x00000000").append(",")
                                    .append("format=rgba,colorchannelmixer=aa=").append(clip.opacity).append(",")
                                    .append("setpts='(PTS-STARTPTS)/").append(clip.speed).append("+").append(clip.startTime).append("/TB'").append(",");
                        }
                        else
                        {
                            // If possible then merge the keyframe to clip
                            clip.mergingVideoPropertiesFromSingleKeyframe();

                            // And then add to filterComplex no matter
                            // the clip has merge or there are no keyframe to combine

                            filterComplex.append("scale=iw*").append(clip.scaleX).append(":ih*").append(clip.scaleY).append(",")
                                    //.append("scale=").append(clip.width).append(":").append(clip.height).append(",")
                                    .append("rotate=").append(radiansRotation).append(":ow=rotw(").append(radiansRotation).append("):oh=roth(").append(radiansRotation).append(")")
                                    .append(":fillcolor=0x00000000").append(",")
                                    .append("format=rgba,colorchannelmixer=aa=").append(clip.opacity).append(",")
                                    .append("setpts='(PTS-STARTPTS)/").append(clip.speed).append("+").append(clip.startTime).append("/TB'").append(",");
                        }


                        filterComplex
                                .append(trimFilter).append(",")
                                // Transition extension: If there has freeze frames, then this line will handle it.
                                .append("tpad=stop_mode=clone:stop_duration=").append(freezeFrameDuration)
                                .append(clipLabel).append(";\n");
                        // TODO: For robust speed control
                        //'
                        //    if(between(T,0,1.5),
                        //       (PTS-STARTPTS)/(1+exp(-k*(T-0.75))),
                        //       if(between(T,1.5,3.5),
                        //          (PTS-STARTPTS)/2,
                        //          (PTS-STARTPTS)/(1+exp(-k*(5-T))))
                        //    ) + 3/TB
                        //'




                        // Transition extension: because overlay are just like transparent layer so we add the raw fillingTransitionDuration
                        filterComplex.append(transparentLabel).append(clipLabel);

                        // In this second if expr: We process posX, posY
                        if (clip.hasAnimatedProperties()) {

                            String posXExpr = getKeyframeFFmpegExpr(clip.keyframes.keyframes, 0, EditingActivity.VideoProperties.ValueType.PosX);
                            String posYExpr = getKeyframeFFmpegExpr(clip.keyframes.keyframes, 0, EditingActivity.VideoProperties.ValueType.PosY);

                            filterComplex.append("overlay='").append(posXExpr).append("':'").append(posYExpr).append("'");
                        }
                        else {
                            // Because we already merged from the first if expr, we don't have to do it here
                            //clip.mergingVideoPropertiesFromSingleKeyframe();


                            filterComplex.append("overlay=").append(clip.posX).append(":").append(clip.posY);

                        }

                        filterComplex.append(":enable='between(t,")
                                .append(clip.startTime).append(",")
                                .append(clip.startTime + clip.duration + fillingTransitionDuration).append(")'").append(",")
                                .append("fps=").append(settings.getFrameRate())
                                .append(outputLabel).append(";\n");

                        tags.storeTag(clip, outputLabel);
                        break;
                    case TEXT:
                        filterComplex.append("[").append(inputIndex).append(":v]")
                                .append("trim=duration=").append(clip.duration).append(",")
                                .append("setpts=PTS-STARTPTS+").append(clip.startTime).append("/TB").append(transparentLabel).append(";\n");

                        filterComplex.append(transparentLabel)
                                .append("drawtext=").append("fontfile='/system/fonts/DroidSans.ttf'")
                                .append(":fontsize=").append(clip.fontSize)
                                .append(":text='").append(clip.textContent.replace(":", "\\:").replace("'", "\\'"))
                                .append("':x=").append("(w-text_w)/2")//.append(clip.posX) Centralize text
                                .append(":y=").append("(h-text_h)/2")//.append(clip.posY) Centralize text
                                .append(":enable='between(t,").append(clip.startTime).append(",")
                                .append(clip.startTime + clip.duration).append(")'").append(",")
                                .append("fps=").append(settings.getFrameRate())
                                .append(outputLabel).append(";\n");

                        tags.storeTag(clip, outputLabel);
                        break;

                    case AUDIO:
                        // 🎵 Pure audio clip logic
                        int delayMs = (int) (clip.startTime * 1000);
                        filterComplex.append("[").append(inputIndex).append(":a]")
                                .append("atrim=start=").append(clip.startClipTrim).append(":end=").append(clip.startClipTrim + clip.duration).append(",")
                                .append("adelay=").append(delayMs).append("|").append(delayMs).append(",")
                                .append("asetpts=PTS-STARTPTS")
                                .append(audioLabel).append(";\n");

                        audioInputs.append(audioLabel);
                        audioClipCount++;
                        break;
                }

                // 🔊 Handle embedded audio in VIDEO
                if (clip.type == EditingActivity.ClipType.VIDEO && clip.isVideoHasAudio && !clip.isMute) {

                    // Transition extension: Same for clip
                    int delayMs = (int) (clip.startTime * 1000);
                    filterComplex.append("[").append(inputIndex).append(":a]")
                            .append("atrim=start=").append(clip.startClipTrim).append(":end=").append(clip.startClipTrim + clip.duration + extendMediaDuration).append(",")
                            .append("adelay=").append(delayMs).append("|").append(delayMs).append(",")
                            // This handle the extension in silent to match the video
                            .append("apad=pad_dur=").append(freezeFrameDuration).append(",")
                            .append("asetpts=PTS-STARTPTS")
                            .append(audioLabel).append(";\n");

                    audioInputs.append(audioLabel);
                    audioClipCount++;
                }

                switch (clip.type) {
                    case VIDEO:
                    case IMAGE:
                    case AUDIO:
                    case TEXT:
                        inputIndex++;
                        break;
                }
            }
        }


        for (EditingActivity.Track track : timeline.tracks) {
            for (EditingActivity.TransitionClip transition : track.transitions) {
                filterComplex.append(FXCommandEmitter.emitTransition(transition, tags));
            }
        }

        for (EditingActivity.Track track : timeline.tracks) {
            for (EditingActivity.Clip clip : track.clips) {

                if (Objects.requireNonNull(clip.type) == EditingActivity.ClipType.EFFECT) {
                    if (clip.effect != null) {
                        filterComplex.append(FXCommandEmitter.emit(clip, tags.useTag(clip), tags)).append("\n");
                    }
                }
            }
        }


        int layer = 0;
        FfmpegFilterComplexTags.FilterComplexInfo baseInfo = tags.useTag(baseTag);
        while(tags.tagsMapToUsableTagIndex.size() > 0)
        {
            EditingActivity.Clip clip = (EditingActivity.Clip) tags.tagsMapToUsableTagIndex.keySet().toArray()[0];

            String prevOutputLabel = "[layer-" + (layer - 1 ) + "]";
            String outputLabel = "[layer-" + layer + "]";

            switch (clip.type) {
                case VIDEO:
                case IMAGE:
                case TEXT:
                    filterComplex.append((layer == 0 ? baseInfo.tag : (tags.useTag(prevOutputLabel).tag))).append(tags.useTag(clip).tag)
                            .append("overlay=")
                            .append("enable='between(t,")
                            .append(clip.startTime).append(",")
                            .append(clip.startTime + clip.duration).append(")'").append(outputLabel).append(";\n");

                    tags.storeTag(outputLabel);

                    layer++;
                    break;
            }
        }

        String finalTag = "";
        layer = 0;

        while(tags.usableTag.size() > 1)
        {
            String outputLabel = "[leftover-layer-" + layer + "]";
            finalTag = tags.usableTag.get(0);
            if(tags.usableTag.size() > 2)
            {
                String upperTag = tags.usableTag.get(1);
                tags.useTag(finalTag);
                tags.useTag(upperTag);
                filterComplex.append(finalTag).append(upperTag)
                        .append("overlay").append(outputLabel).append(";\n");
                tags.storeTag(outputLabel);
            }
            else break;
        }


        // 🔁 Mix audio if present
        if (audioClipCount > 0) {
            filterComplex.append(audioInputs)
                    .append("amix=inputs=").append(audioClipCount).append(":dropout_transition=0").append("[aout];\n");
            audioMaps.append("-map \"[aout]\" ");
        } else {
            audioMaps.append("-an "); // 🧘 No audio at all
        }

        // Available when the track has at least 1 video
        // Null when there are no video in the track
        FfmpegFilterComplexTags.FilterComplexInfo mapTag = tags.useTag(0);

        cmd.append("-filter_complex \"").append(filterComplex).append("\" ")
                .append("-map \"").append( (mapTag != null ? mapTag.tag : "[base]") ).append("\" ")
                .append(audioMaps)
                .append("-t ").append(timeline.duration)
                .append(" -c:v libx264 -preset ").append(settings.getPreset())
                .append(" -tune ").append(settings.getTune())
                .append(" -crf ").append(settings.getCRF())
                .append(" -y ").append("\"").append(IOHelper.CombinePath(data.getProjectPath(), Constants.DEFAULT_EXPORT_CLIP_FILENAME)).append("\"");

        return cmd.toString();
    }


    /**
     *
     * Get the Expr for keyframe rendering.
     * @param keyframes Total keyframe of the Clip
     * @param startIndex Index for prevKeyframe. Put 0 for start.
     * @param valueType (scaleX, scaleY, posX, posY, Rot)
     * @return Expression for FFmpeg in String format.
     *
     */
    public static String getKeyframeFFmpegExpr(List<EditingActivity.Keyframe> keyframes, int startIndex, EditingActivity.VideoProperties.ValueType valueType)
    {
        StringBuilder keyframeExprString = new StringBuilder();

        if(startIndex + 1 >= keyframes.size()) return "1"; // Default value
        // TODO: Set the default value to match the current clip properties. Need rework on the structure.

        EditingActivity.Keyframe prevKeyframe = keyframes.get(startIndex);
        EditingActivity.Keyframe nextKeyframe = keyframes.get(startIndex + 1);


        keyframeExprString
                .append("if(")
                .append("gte(t,").append(prevKeyframe.time).append(")")
                .append("*")
                .append("lte(t,").append(nextKeyframe.time).append(")").append(",")
                .append(nextKeyframe.value.getValue(valueType)).append(",")
                .append(getKeyframeFFmpegExpr(keyframes, startIndex + 1, valueType))
                .append(")");

        return keyframeExprString.toString();
    }









    public static class FfmpegFilterComplexTags {
        private final ArrayList<String> usableTag = new ArrayList<>();
        private final Map<EditingActivity.Clip, String> tagsMapToUsableTagIndex = new HashMap<>();
        private final Map<EditingActivity.Clip, EditingActivity.Clip> tagsMergedClipMap = new HashMap<>();

        public FilterComplexInfo useTag(int index) {
            if(index < 0 || index >= usableTag.size()) return null;

            String retrieveTag = usableTag.get(index);
            FilterComplexInfo info = new FilterComplexInfo(index, retrieveTag);

            usableTag.remove(index);
            return info;
        }
        public FilterComplexInfo useTag(String tag) {
            if(usableTag.contains(tag))
            {
                int indexTag = usableTag.indexOf(tag);
                FilterComplexInfo info = new FilterComplexInfo(indexTag, tag);
                usableTag.remove(indexTag);
                return info;
            }
            return null;
        }
        public void storeTag(String tag) {
            usableTag.add(tag);
        }
        public void storeTag(String tag, int index) {
            usableTag.add(index, tag);
        }


        public FilterComplexInfo useTag(EditingActivity.Clip key) {
            if(usableTag.contains(tagsMapToUsableTagIndex.get(key)))
            {
                String retrieveTag = tagsMapToUsableTagIndex.get(key);
                int indexTag = usableTag.indexOf(retrieveTag);
                FilterComplexInfo info = new FilterComplexInfo(indexTag, retrieveTag);

                usableTag.remove(indexTag);
                tagsMapToUsableTagIndex.remove(key);
                return info;
            }
//            else if(tagsMergedClipMap.containsKey(key) && usableTag.contains(tagsMapToUsableTagIndex.get(tagsMergedClipMap.get(key))))
//            {
//                useTag(tagsMergedClipMap.get(key));
//                tagsMergedClipMap.remove(key);
//            }
            return null;
        }
        public FilterComplexInfo useTag(EditingActivity.Clip key, EditingActivity.Clip mergingKey) {
            if(usableTag.contains(tagsMapToUsableTagIndex.get(key)))
            {
                String retrieveTag = tagsMapToUsableTagIndex.get(key);
                int indexTag = usableTag.indexOf(retrieveTag);
                FilterComplexInfo info = new FilterComplexInfo(indexTag, retrieveTag);

                usableTag.remove(indexTag);
                tagsMapToUsableTagIndex.remove(key);

                tagsMergedClipMap.put(key, mergingKey);
                return info;
            }
            return null;
        }
        public void storeTag(EditingActivity.Clip key, String tag) {
            usableTag.add(tag);

            tagsMapToUsableTagIndex.put(key, tag);
        }
        public void storeTag(EditingActivity.Clip key, String tag, int index) {
            if(index < 0) index = 0;
            if(index >= usableTag.size()) index = usableTag.size() - 1;
            usableTag.add(index, tag);

            tagsMapToUsableTagIndex.put(key, tag);
        }


        public EditingActivity.Clip getKeyFromTag(String tag)
        {
            for (Map.Entry<EditingActivity.Clip, String> entry : tagsMapToUsableTagIndex.entrySet()) {
                if (entry.getValue().equals(tag)) {
                    return entry.getKey();
                }
            }
            return null;
        }


        public EditingActivity.Clip getValidMapKey(EditingActivity.Clip clipKey)
        {
            if(tagsMapToUsableTagIndex.containsKey(clipKey))
                return clipKey;
            if(tagsMergedClipMap.containsKey(clipKey))
                return tagsMergedClipMap.get(clipKey);
            return null;
        }




        static class FilterComplexInfo {
            public int index;
            public String tag;
            public FilterComplexInfo(int index, String tag)
            {
                this.index = index;
                this.tag = tag;
            }
        }
    }

    public static class FfmpegRenderQueue {
        public FfmpegRenderQueueInfo currentRenderQueue;
        private final Queue<FfmpegRenderQueueInfo> taskQueue = new LinkedList<>();
        public boolean isRunning = false;

        public int totalQueue = 0, queueDone = 0;

        public void enqueue(FfmpegRenderQueueInfo task) {
            taskQueue.add(task);
            totalQueue++;
            if (!isRunning) {
                runNext();
            }
        }

        private void runNext() {
            FfmpegRenderQueueInfo task = taskQueue.poll();
            if (task == null) {
                isRunning = false;
                totalQueue = 0;
                queueDone = 0;
                return;
            }
            currentRenderQueue = task;

            isRunning = true;
            queueDone++;
            task.task.run(); // Each task must call runNext() when done
        }

        public void taskCompleted() {
            runNext();
        }
        public void cancelAllTask()
        {
            isRunning = false;
            taskQueue.clear();
            totalQueue = 0;
            queueDone = 0;
            FFmpegKit.cancel();
        }




        public static class FfmpegRenderQueueInfo {
            Runnable task;
            public String taskName;



            RunnableImpl onStatisticBase = new RunnableImpl() {
                @Override
                public <T> void runWithParam(T param) {
                    Statistics stats = (Statistics) param;

//                    double duration = stats.get;

                    Statistics statistics = (Statistics) param;
                    {
                        if (statistics.getTime() > 0) {
//                            int progress = (int) ((statistics.getTime() * 100) / (int) duration);
//                            statusBar.setMax(100);
//                            statusBar.setProgress(progress);
                        }
                    }
                }
            };
            RunnableImpl onLog;

            public FfmpegRenderQueueInfo(String taskName, Runnable task)
            {
                this.task = task;
                this.taskName = taskName;
            }



        }

    }

}