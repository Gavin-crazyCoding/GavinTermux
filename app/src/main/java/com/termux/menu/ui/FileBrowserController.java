package com.termux.menu.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.termux.menu.R;
import com.termux.menu.utils.FileUtils;

import java.io.File;
import java.util.Stack;

public class FileBrowserController implements FileBrowserAdapter.FileActionListener {
    private View mRootView;
    private RecyclerView mFileList;
    private TextView mPathText;
    private TextView mNewFileBtn;
    private FileBrowserAdapter mAdapter;
    private PageController mPageController;
    private EditorController mEditorController;
    private Stack<String> mPathStack;
    private String mCurrentPath;
    private Context mContext;

    public FileBrowserController(View rootView, PageController pageController, EditorController editorController) {
        mRootView = rootView;
        mPageController = pageController;
        mEditorController = editorController;
        mContext = rootView.getContext();

        mFileList = rootView.findViewById(R.id.fb_list);
        mPathText = rootView.findViewById(R.id.fb_path);
        mNewFileBtn = rootView.findViewById(R.id.fb_newfile_btn);

        mFileList.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new FileBrowserAdapter(mContext, this);
        mFileList.setAdapter(mAdapter);
        mPathStack = new Stack<>();

        mNewFileBtn.setOnClickListener(v -> showNewFileDialog());
    }

    public void openDirectory(String path) {
        mCurrentPath = path;
        mPathText.setText(shortenPath(path));
        mPageController.setTitle("文件浏览");
        File[] files = FileUtils.listFilesSorted(path);
        mAdapter.setFiles(files);
        mPageController.showPage(PageController.PAGE_FILEBROWSER);
    }

    public void navigateTo(String path) {
        if (mCurrentPath != null) mPathStack.push(mCurrentPath);
        openDirectory(path);
    }

    public boolean goUp() {
        if (!mPathStack.isEmpty()) {
            String prev = mPathStack.pop();
            mCurrentPath = prev;
            mPathText.setText(shortenPath(prev));
            File[] files = FileUtils.listFilesSorted(prev);
            mAdapter.setFiles(files);
            return true;
        }
        File parent = new File(mCurrentPath).getParentFile();
        if (parent != null && parent.exists() && !mCurrentPath.equals("/")) {
            mCurrentPath = parent.getAbsolutePath();
            mPathText.setText(shortenPath(mCurrentPath));
            File[] files = FileUtils.listFilesSorted(mCurrentPath);
            mAdapter.setFiles(files);
            return true;
        }
        return false;
    }

    @Override
    public void onFileClick(File file) {
        mEditorController.openFile(file.getAbsolutePath());
    }

    @Override
    public void onFolderClick(File folder) {
        navigateTo(folder.getAbsolutePath());
    }

    @Override
    public void onFileLongClick(File file) {
        String[] options = {"用编辑器打开", "删除", "重命名"};
        AlertDialog dialog = new AlertDialog.Builder(mContext)
            .setTitle(file.getName())
            .setItems(options, (d, which) -> {
                switch (which) {
                    case 0:
                        mEditorController.openFile(file.getAbsolutePath());
                        break;
                    case 1:
                        confirmDelete(file);
                        break;
                    case 2:
                        showRenameDialog(file);
                        break;
                }
            })
            .create();
        applyDialogType(dialog);
        dialog.show();
    }

    private void confirmDelete(File file) {
        AlertDialog dialog = new AlertDialog.Builder(mContext)
            .setTitle("确认删除")
            .setMessage("是否删除 " + file.getName() + "？")
            .setPositiveButton("删除", (d, w) -> {
                boolean ok = deleteRecursive(file);
                Toast.makeText(mContext, ok ? "已删除" : "删除失败", Toast.LENGTH_SHORT).show();
                refreshCurrentDir();
            })
            .setNegativeButton("取消", null)
            .create();
        applyDialogType(dialog);
        dialog.show();
    }

    private boolean deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) deleteRecursive(child);
            }
        }
        return file.delete();
    }

    private void showRenameDialog(File file) {
        EditText input = new EditText(mContext);
        input.setText(file.getName());
        input.selectAll();
        AlertDialog dialog = new AlertDialog.Builder(mContext)
            .setTitle("重命名")
            .setView(input)
            .setPositiveButton("确定", (d, w) -> {
                String newName = input.getText().toString().trim();
                if (!newName.isEmpty()) {
                    File newFile = new File(file.getParent(), newName);
                    boolean ok = file.renameTo(newFile);
                    Toast.makeText(mContext, ok ? "已重命名" : "重命名失败", Toast.LENGTH_SHORT).show();
                    refreshCurrentDir();
                }
            })
            .setNegativeButton("取消", null)
            .create();
        applyDialogType(dialog);
        dialog.show();
    }

    private void showNewFileDialog() {
        String[] options = {"新建文件", "新建文件夹"};
        AlertDialog dialog = new AlertDialog.Builder(mContext)
            .setTitle("新建")
            .setItems(options, (d, which) -> {
                EditText input = new EditText(mContext);
                input.setHint(which == 0 ? "文件名" : "文件夹名");
                AlertDialog inner = new AlertDialog.Builder(mContext)
                    .setTitle(options[which])
                    .setView(input)
                    .setPositiveButton("创建", (d2, w2) -> {
                        String name = input.getText().toString().trim();
                        if (name.isEmpty()) return;
                        File target = new File(mCurrentPath, name);
                        boolean ok;
                        if (which == 0) {
                            ok = FileUtils.writeFile(target.getAbsolutePath(), "");
                        } else {
                            ok = target.mkdirs();
                        }
                        Toast.makeText(mContext, ok ? "已创建" : "创建失败", Toast.LENGTH_SHORT).show();
                        refreshCurrentDir();
                    })
                    .setNegativeButton("取消", null)
                    .create();
                applyDialogType(inner);
                inner.show();
            })
            .create();
        applyDialogType(dialog);
        dialog.show();
    }

    private void refreshCurrentDir() {
        if (mCurrentPath != null) {
            mPathText.setText(shortenPath(mCurrentPath));
            mAdapter.setFiles(FileUtils.listFilesSorted(mCurrentPath));
        }
    }

    private String shortenPath(String path) {
        if (path.startsWith(FileUtils.TERMUX_HOME)) {
            return "~" + path.substring(FileUtils.TERMUX_HOME.length());
        }
        return path;
    }

    private void applyDialogType(AlertDialog dialog) {
        if (dialog.getWindow() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            } else {
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
            }
        }
    }
}
