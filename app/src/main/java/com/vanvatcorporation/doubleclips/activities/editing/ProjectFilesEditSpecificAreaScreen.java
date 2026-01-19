package com.vanvatcorporation.doubleclips.activities.editing;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.vanvatcorporation.doubleclips.FFmpegEdit;
import com.vanvatcorporation.doubleclips.R;
import com.vanvatcorporation.doubleclips.activities.EditingActivity;
import com.vanvatcorporation.doubleclips.activities.main.MainAreaScreen;
import com.vanvatcorporation.doubleclips.constants.Constants;
import com.vanvatcorporation.doubleclips.helper.AlgorithmHelper;
import com.vanvatcorporation.doubleclips.helper.IOHelper;
import com.vanvatcorporation.doubleclips.helper.IOImageHelper;
import com.vanvatcorporation.doubleclips.helper.ImageHelper;
import com.vanvatcorporation.doubleclips.manager.LoggingManager;

import java.io.File;
import java.io.Serializable;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

public class ProjectFilesEditSpecificAreaScreen extends BaseEditSpecificAreaScreen {
    public EditingActivity activityInstance;
    public MainAreaScreen.ProjectData properties;

    public List<ProjectFilesData> projectFilesList;
    public RecyclerView projectFilesRecyclerView;
    public ProjectFilesAdapter projectFilesAdapter;
    public SwipeRefreshLayout projectFilesSwipeRefreshLayout;


    public EditText hexColorField;



    public ProjectFilesEditSpecificAreaScreen(Context context) {
        super(context);
    }

    public ProjectFilesEditSpecificAreaScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProjectFilesEditSpecificAreaScreen(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ProjectFilesEditSpecificAreaScreen(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void init()
    {
        super.init();

        hexColorField = findViewById(R.id.hexColorField);

        projectFilesRecyclerView = findViewById(R.id.projectFilesRecyclerView);
        projectFilesSwipeRefreshLayout = findViewById(R.id.projectFilesSwipeRefreshLayout);


        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        projectFilesRecyclerView.setLayoutManager(layoutManager);


        projectFilesList = new ArrayList<>();
        projectFilesAdapter = new ProjectFilesAdapter(getContext(), projectFilesList);
        projectFilesRecyclerView.setAdapter(projectFilesAdapter);

        projectFilesSwipeRefreshLayout.setOnRefreshListener(this::reloadingProject);

        findViewById(R.id.createButton).setOnClickListener(v -> {
            FFmpegEdit.generateSolidColorImage(getContext(), IOHelper.CombinePath(properties.getProjectPath(), Constants.DEFAULT_CLIP_DIRECTORY), hexColorField.getText().toString());
        });

        onClose.add(() -> {
            hexColorField.clearFocus();
        });
        onOpen.add(() -> {
            projectFilesSwipeRefreshLayout.setRefreshing(true);
            reloadingProject();
        });

        animationScreen = AnimationScreen.ToBottom;
    }
    public void reloadingProject()
    {
        projectFilesList.clear();
        projectFilesAdapter.notifyDataSetChanged();

        fetchClipsFolder();
    }

    public void fetchClipsFolder()
    {
        if(properties == null) {
            projectFilesSwipeRefreshLayout.setRefreshing(false);
            return;
        }

        File file = new File(IOHelper.CombinePath(properties.getProjectPath(), Constants.DEFAULT_CLIP_DIRECTORY));
        if (file.isDirectory()) {
            for (String child : Objects.requireNonNull(file.list())) {

                String childPath = IOHelper.CombinePath(properties.getProjectPath(), Constants.DEFAULT_CLIP_DIRECTORY, child);
                if(new File(childPath).isDirectory()) continue;

                Bitmap previewBitmap;

                String mimeType = URLConnection.guessContentTypeFromName(child);


                if (mimeType.startsWith("audio/")) {
                    previewBitmap = ImageHelper.createBitmapFromDrawable( ResourcesCompat.getDrawable(getResources(), R.drawable.baseline_audio_file_24, null));
                }
                else if (mimeType.startsWith("image/")) {
                    previewBitmap = IOImageHelper.LoadFileAsPNGImage(getContext(), childPath, Constants.SAMPLE_SIZE_PREVIEW_CLIP);
                }
                else if (mimeType.startsWith("video/")) {
                    try {
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(childPath);
                        int originalWidth = Integer.parseInt(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)));
                        int originalHeight = Integer.parseInt(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)));

                        int desiredWidth = originalWidth / Constants.SAMPLE_SIZE_PREVIEW_CLIP;
                        int desiredHeight = originalHeight / Constants.SAMPLE_SIZE_PREVIEW_CLIP;
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                            previewBitmap = retriever.getScaledFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, desiredWidth, desiredHeight);
                        }
                        else {
                            previewBitmap = Bitmap.createScaledBitmap(
                                    Objects.requireNonNull(retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)),
                                    desiredWidth, desiredHeight, true);

                        }

                        retriever.release();
                        retriever.close();
                    } catch (Exception e) {
                        LoggingManager.LogExceptionToNoteOverlay(getContext(), e);
                        previewBitmap = null;
                    }
                }
                else {
                    previewBitmap = ImageHelper.createBitmapFromDrawable( ResourcesCompat.getDrawable(getResources(), R.drawable.baseline_question_mark_24, null));
                }; // if effect or unknown






                projectFilesList.add(new ProjectFilesData(childPath, child, previewBitmap));
                projectFilesAdapter.notifyItemInserted(projectFilesList.size() - 1);
            }
        }

        projectFilesSwipeRefreshLayout.setRefreshing(false);
    }








    public static class ProjectFilesData implements Serializable {

        public String filePath;
        public String fileTitle;
        public Bitmap filePreview;

        public ProjectFilesData(String filePath, String fileTitle, Bitmap filePreview) {
            this.filePath = filePath;
            this.fileTitle = fileTitle;
            this.filePreview = filePreview;
        }


        public String getFilePath() {
            return filePath;
        }
        public String getFileTitle() {
            return fileTitle;
        }
        public Bitmap getFilePreview() {
            return filePreview;
        }


        public void setFileTitle(String fileTitle) {
            this.fileTitle = fileTitle;
        }
        public void setFilePreview(Bitmap filePreview) {
            this.filePreview = filePreview;
        }

    }
    public class ProjectFilesAdapter extends RecyclerView.Adapter<ProjectFilesViewHolder>
    {

        private List<ProjectFilesData> projectFilesList;
        private Context context;

        // Constructor
        public ProjectFilesAdapter(Context context, List<ProjectFilesData> projectFilesList) {
            this.context = context;
            this.projectFilesList = projectFilesList;
        }
        @Override
        public ProjectFilesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.cpn_project_files_element, parent, false);
            return new ProjectFilesViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProjectFilesViewHolder holder, int position) {

            ProjectFilesData projectItem = projectFilesList.get(position);

            holder.projectFilesTitle.setText(projectItem.getFileTitle());

            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    Bitmap thumbnailBitmap = projectItem.getFilePreview();

                    if(thumbnailBitmap != null) {
                        holder.wholeView.post(() -> {
                            holder.projectFilesPreview.setImageBitmap(thumbnailBitmap);
                            int targetWidth = holder.wholeView.getWidth();

                            int imageWidth = thumbnailBitmap.getWidth();
                            int imageHeight = thumbnailBitmap.getHeight();

                            int[] res = AlgorithmHelper.scaleByWidth(targetWidth, imageWidth, imageHeight);

                            ViewGroup.LayoutParams imageDimension = holder.projectFilesPreview.getLayoutParams();
                            imageDimension.width = res[0];
                            imageDimension.height = res[1];
                            holder.projectFilesPreview.setLayoutParams(imageDimension);
                        });
                    }
                } catch (Exception e) {
                    LoggingManager.LogExceptionToNoteOverlay(context, e);
                }
            });

            holder.projectFilesTitle.setOnClickListener(v -> {
                holder.wholeView.performClick();
            });

            holder.wholeView.setOnClickListener(v -> {
                if(activityInstance != null)
                    if(EditingActivity.selectedTrack != null) {
                        activityInstance.addProjectFileMediaToTrack(projectItem.getFileTitle(), projectItem.getFilePath());
                    }
                    else new AlertDialog.Builder(getContext()).setTitle("Error").setMessage("You need to pick a track first!").show();
                    else new AlertDialog.Builder(getContext()).setTitle("Error").setMessage("Tung Tung Tung Sahur! (EditingActivity instance not initialized!)").show();
            });
            holder.wholeView.setOnLongClickListener(v -> {


                {
                    PopupMenu popup = new PopupMenu(context, v);
                    popup.getMenuInflater().inflate(R.menu.menu_cpn_project_files_more, popup.getMenu());

                    popup.setOnMenuItemClickListener(item -> {
                        if(item.getItemId() == R.id.action_edit)
                        {
                            editProjectFileName(properties, projectItem);
                            return true;
                        }
                        else if(item.getItemId() == R.id.action_delete)
                        {
                            new AlertDialog.Builder(context)
                                    .setTitle(context.getString(R.string.alert_delete_media_confirmation_title))
                                    .setMessage(context.getString(R.string.alert_delete_media_confirmation_description))

                                    // Specifying a listener allows you to take an action before dismissing the dialog.
                                    // The dialog is automatically dismissed when a dialog button is clicked.
                                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                        // Continue with delete operation
                                        activityInstance.deleteAllClipWithName(projectItem.getFileTitle());

                                        IOHelper.deleteFile(IOHelper.CombinePath(properties.getProjectPath(), Constants.DEFAULT_CLIP_DIRECTORY, projectItem.getFileTitle()));
                                        IOHelper.deleteFile(IOHelper.CombinePath(properties.getProjectPath(), Constants.DEFAULT_PREVIEW_CLIP_DIRECTORY, projectItem.getFileTitle()));
                                        projectFilesList.remove(position);
                                        notifyItemRemoved(position);

                                    })

                                    // A null listener allows the button to dismiss the dialog and take no further action.
                                    .setNegativeButton(android.R.string.cancel, null)
                                    .setIconAttribute(android.R.attr.alertDialogIcon)
                                    .show();
                            return true;
                        }
                        else if(item.getItemId() == R.id.action_swap)
                        {

                            //activityInstance.renameProjectFile(projectFile.getFileTitle(), editText.getText().toString());
                            // TODO: Swapping the clip
                            //  add Change File (First rename the Clip name to the Imported Filename,
                            //  second import the filename, and go through the preview process,
                            //  then check if the filename and old file are matched,
                            //  if old filename and new filename are the same,
                            //  then we will not need to delete the file, else we will delete the old file)



                            return true;
                        }
                        return false;
                    });

                    popup.show();
                }

                return true;
            });
        }


        private void editProjectFileName(MainAreaScreen.ProjectData projectItem, ProjectFilesData projectFile) {

            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            // Inflate your custom layout
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.popup_edit_project_filename, null);
            builder.setView(dialogView);

            // Get references to the EditText and Buttons in your custom layout
            EditText editText = dialogView.findViewById(R.id.directoryText);
            Button okButton = dialogView.findViewById(R.id.okButton);
            Button cancelButton = dialogView.findViewById(R.id.cancelButton);

            // Create the AlertDialog
            AlertDialog dialog = builder.create();
            editText.setText(projectFile.getFileTitle());

            // Set button click listeners
            okButton.setOnClickListener(vok -> {
                activityInstance.renameProjectFile(projectFile.getFileTitle(), editText.getText().toString());

                dialog.dismiss();
            });

            cancelButton.setOnClickListener(vcan -> {
                // Just dismiss the dialog
                dialog.dismiss();
            });

            // Show the dialog
            dialog.show();


        }


        @Override
        public int getItemCount() {
            return projectFilesList.size();
        }
    }
    public static class ProjectFilesViewHolder extends RecyclerView.ViewHolder {
        TextView projectFilesTitle;
        ImageView projectFilesPreview;
        View wholeView;
        public ProjectFilesViewHolder(@NonNull View itemView) {
            super(itemView);
            wholeView = itemView;

            projectFilesTitle = itemView.findViewById(R.id.titleText);
            projectFilesPreview = itemView.findViewById(R.id.previewImage);
        }
    }

}
