package com.vanvatcorporation.doubleclips.activities.main;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.vanvatcorporation.doubleclips.BuildConfig;
import com.vanvatcorporation.doubleclips.R;
import com.vanvatcorporation.doubleclips.activities.TemplatePreviewActivity;
import com.vanvatcorporation.doubleclips.externalUtils.Random;
import com.vanvatcorporation.doubleclips.helper.ImageHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TemplateAreaScreen extends BaseAreaScreen {
    public List<TemplateData> templateList;
    public RecyclerView templateRecyclerView;
    public TemplateDataAdapter templateAdapter;
    public SwipeRefreshLayout templateSwipeRefreshLayout;



    public TemplateAreaScreen(Context context) {
        super(context);
    }

    public TemplateAreaScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TemplateAreaScreen(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TemplateAreaScreen(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public void init() {
        super.init();

        templateRecyclerView = findViewById(R.id.templateRecyclerView);
        templateSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);




        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        templateRecyclerView.setLayoutManager(layoutManager);


        templateList = new ArrayList<>();
        templateAdapter = new TemplateDataAdapter(getContext(), templateList);
        templateRecyclerView.setAdapter(templateAdapter);

        templateSwipeRefreshLayout.setOnRefreshListener(this::reloadingProject);


    }



    public void addTemplate(TemplateData data)
    {
        templateList.add(data);
        templateAdapter.notifyItemInserted(templateList.size() - 1);
    }

    public void fetchTemplate() {

        // TODO: Fetch from real server. This is for example purpose.
        TemplateData data = new TemplateData(
                "Tớ yêu cậu Template",
                "Template tớ yêu cậu là template đầu tiên trong hệ sinh thái. Ra mắt vào ngày 28/12/2025, Template tớ yêu cậu đã đánh dấu sự ra đời của hệ thống mẫu chỉnh sửa video mã nguồn mở.",
                "-i \"<editable-video-0>\" -y output.mp4",
                "https://app.vanvatcorp.com/doubleclips/templates/viet2007ht/mkr5r-SDfve6/preview.png",
                "https://app.vanvatcorp.com/doubleclips/templates/viet2007ht/mkr5r-SDfve6/preview.mp4",
                new Date().getTime(), 8032007);
        addTemplate(data);

//        TemplateData data = new TemplateData(projectPath, projectName, new Date().getTime(), 31122007, 8032007);
//        data.version = BuildConfig.VERSION_NAME;
//        projectList.add(data);
//        projectAdapter.notifyItemInserted(projectList.size() - 1);
//
//        File basicDir = new File(IOHelper.CombinePath(projectPath, Constants.DEFAULT_CLIP_TEMP_DIRECTORY, "frames"));
//        if(!basicDir.exists())
//            basicDir.mkdirs();
//
//        File previewDir = new File(IOHelper.CombinePath(projectPath, Constants.DEFAULT_PREVIEW_CLIP_DIRECTORY));
//        if(!previewDir.exists())
//            previewDir.mkdirs();
//
//        enterEditing(getContext(), data);
    }

    public void reloadingProject()
    {
        // TODO: Reload from real server, this serve as example purpose only.
        fetchTemplate();


//        projectList.clear();
//        projectAdapter.notifyDataSetChanged();
//        String projectsFolderPath = IOHelper.CombinePath(IOHelper.getPersistentDataPath(getContext()), "projects");
//        File file = new File(projectsFolderPath);
//        if(file.listFiles() == null) {
//            projectSwipeRefreshLayout.setRefreshing(false);
//            return;
//        }
//        for (File directory : Objects.requireNonNull(file.listFiles())) {
//            if(directory.isDirectory())
//            {
//                TemplateData data = TemplateData.loadProperties(getContext(), directory.getAbsolutePath());
//
//                if(data != null)
//                {
//                    projectList.add(data);
//                    projectAdapter.notifyItemInserted(projectList.size() - 1);
//                }
//            }
//
//        }
//
        templateSwipeRefreshLayout.setRefreshing(false);
    }










    public static class TemplateData implements Serializable {

        public String version;
        private String projectTitle;
        private String projectDescription;
        private String ffmpegCommand;
        private String templateSnapshotLink;
        private String templateVideoLink;
        private long templateTimestamp;
        private long templateDuration;

        public TemplateData(String projectTitle, String projectDescription, String ffmpegCommand, String templateSnapshotLink, String templateVideoLink, long templateTimestamp, long templateDuration) {
            this.projectTitle = projectTitle;
            this.projectDescription = projectDescription;
            this.ffmpegCommand = ffmpegCommand;
            this.templateSnapshotLink = templateSnapshotLink;
            this.templateVideoLink = templateVideoLink;
            this.templateTimestamp = templateTimestamp;
            this.templateDuration = templateDuration;
        }


        public String getProjectTitle() {
            return projectTitle;
        }
        public String getProjectDescription() {
            return projectDescription;
        }
        public String getFfmpegCommand() {
            return ffmpegCommand;
        }
        public String getTemplateSnapshotLink() {
            return templateSnapshotLink;
        }
        public String getTemplateVideoLink() {
            return templateVideoLink;
        }
        public long getTemplateTimestamp() {
            return templateTimestamp;
        }
        public long getTemplateDuration() {
            return templateDuration;
        }
    }
    public class TemplateDataAdapter extends RecyclerView.Adapter<TemplateDataViewHolder>
    {

        private List<TemplateData> templateList;
        private Context context;

        // Constructor
        public TemplateDataAdapter(Context context, List<TemplateData> templateList) {
            this.context = context;
            this.templateList = templateList;
        }
        @Override
        public TemplateDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.cpn_template_element, parent, false);
            return new TemplateDataViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TemplateDataViewHolder holder, int position) {

            TemplateData projectItem = templateList.get(position);

            holder.templateTitle.setText(projectItem.getProjectTitle());

            ImageHelper.getImageBitmapFromNetwork(context, projectItem.getTemplateSnapshotLink(), holder.templatePreview);
            ViewGroup.LayoutParams imageDimension = holder.templatePreview.getLayoutParams();
            imageDimension.width = ViewGroup.LayoutParams.MATCH_PARENT;
            imageDimension.height = Random.Range(100, 600);
            holder.templatePreview.setLayoutParams(imageDimension);
            holder.templateTitle.setOnClickListener(v -> {
                holder.wholeView.performClick();
            });


            holder.wholeView.setOnClickListener(v -> {
                Intent intent = new Intent(context, TemplatePreviewActivity.class);
                intent.putExtra("TemplateData", projectItem);
                context.startActivity(intent);
            });
            holder.wholeView.setOnLongClickListener(v -> {
                return true;
            });
        }


        @Override
        public int getItemCount() {
            return templateList.size();
        }
    }
    public static class TemplateDataViewHolder extends RecyclerView.ViewHolder {
        TextView templateTitle;
        ImageView templatePreview;
        View wholeView;
        public TemplateDataViewHolder(@NonNull View itemView) {
            super(itemView);
            wholeView = itemView;

            templateTitle = itemView.findViewById(R.id.titleText);
            templatePreview = itemView.findViewById(R.id.previewImage);
        }
    }
}
