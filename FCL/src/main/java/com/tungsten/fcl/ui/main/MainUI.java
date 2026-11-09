package com.tungsten.fcl.ui.main;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mio.skin.AnimationDialog;
import com.mio.skin.SkinRenderer;
import com.mio.skin.SkinTextureLoader;
import com.mio.skin.SkinViewer;

import static com.mio.skin.SkinAnimationsKt.restoreSkinSettings;
import static com.mio.skin.SkinAnimationsKt.saveSkinSettings;
import com.tungsten.fcl.R;
import com.tungsten.fcl.news.MinecraftNews;
import com.tungsten.fcl.news.MinecraftNewsApi;
import com.tungsten.fcl.setting.Accounts;
import com.tungsten.fclcore.auth.Account;
import com.tungsten.fclcore.fakefx.beans.InvalidationListener;
import com.tungsten.fclcore.task.Schedulers;
import com.tungsten.fclcore.util.Logging;
import com.tungsten.fcllibrary.component.theme.ThemeEngine;
import com.tungsten.fcllibrary.component.ui.FCLCommonUI;

import java.util.logging.Level;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainUI extends FCLCommonUI {

    private LinearLayoutCompat newsContainer;
    private LinearLayoutCompat newsLayout;

    private final MinecraftNewsAdapter newsAdapter = new MinecraftNewsAdapter();

    private SkinViewer skinViewer;
    private SkinRenderer renderer;
    private SkinTextureLoader skinLoader;

    /** 选中账户变化时重载皮肤（attach 时注册、detach 时注销） */
    private final InvalidationListener accountListener = o ->
            skinLoader.load(Accounts.getSelectedAccount(), false);

    public MainUI(Context context, int id) {
        super(context, id);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        newsContainer = findViewById(R.id.news_container);
        newsLayout = findViewById(R.id.news_layout);
        RecyclerView newsList = findViewById(R.id.news_list);
        newsList.setLayoutManager(new LinearLayoutManager(getContext()));
        newsList.setAdapter(newsAdapter);
        ThemeEngine.getInstance().registerEvent(newsLayout, () -> newsLayout.getBackground().setTint(ThemeEngine.getInstance().getTheme().getColor()));

        skinViewer = findViewById(R.id.skin_viewer);
        renderer = new SkinRenderer(getContext());
        skinViewer.setRenderer(renderer, 5f);
        skinLoader = new SkinTextureLoader(renderer);
        skinLoader.load(Accounts.getSelectedAccount(), false);
        // 恢复上次选择的动画与 3D 皮肤层开关
        restoreSkinSettings(getContext(), renderer);
        // 双击模型弹出皮肤模型设置窗口
        skinViewer.setOnDoubleClick(() -> {
            Context context = getContext();
            if (context instanceof Activity && !((Activity) context).isDestroyed() && !((Activity) context).isFinishing()) {
                new AnimationDialog(context, renderer.getAnimation(), renderer.getOverlayEnabled(), animation -> {
                    renderer.playAnimation(animation);
                    saveSkinSettings(context, renderer);
                }, enabled -> {
                    renderer.setOverlayEnabled(enabled);
                    saveSkinSettings(context, renderer);
                }).show();
            }
        });
        loadNews();

        // 皮肤渲染随页面挂载/回收恢复与暂停（替代原 onStart/onStop 生命周期）
        getContentView().addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
                if (skinViewer != null) {
                    if (!ThemeEngine.getInstance().getTheme().isCloseSkinModel()) {
                        skinViewer.setVisibility(View.VISIBLE);
                        skinViewer.onResume();
                        // 纹理由渲染线程 onSurfaceCreated 从 renderer.texture 自行重建，
                        // 此处不重喂纹理；同账户已在 onCreate 加载过，load 会自动跳过
                    } else {
                        skinViewer.onPause();
                        skinViewer.setVisibility(View.GONE);
                    }
                }
                if (skinLoader != null) {
                    // detach 期间选中的账户可能已切换（监听已注销），重新对齐一次
                    Accounts.selectedAccountProperty().addListener(accountListener);
                    skinLoader.load(Accounts.getSelectedAccount(), false);
                }
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
                Accounts.selectedAccountProperty().removeListener(accountListener);
                if (skinLoader != null) {
                    skinLoader.release();
                }
                if (skinViewer != null) {
                    skinViewer.onPause();
                    skinViewer.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (skinViewer != null) {
            skinViewer.onPause();
            skinViewer.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (skinViewer != null && isShowing() && !ThemeEngine.getInstance().getTheme().isCloseSkinModel()) {
            skinViewer.setVisibility(View.VISIBLE);
            skinViewer.onResume();
        }
    }

    /**
     * 拉取官方 Minecraft 新闻并展示；请求失败或没有条目时保持卡片隐藏，
     * 主页没有新闻卡片时不影响其余内容。
     */
    private void loadNews() {
        MinecraftNewsApi.create().getNews().enqueue(new Callback<MinecraftNews>() {
            @Override
            public void onResponse(@NonNull Call<MinecraftNews> call, @NonNull Response<MinecraftNews> response) {
                MinecraftNews news = response.body();
                if (!response.isSuccessful() || news == null || news.getEntries().isEmpty())
                    return;
                newsContainer.setVisibility(View.VISIBLE);
                newsAdapter.submit(news.getEntries());
            }

            @Override
            public void onFailure(@NonNull Call<MinecraftNews> call, @NonNull Throwable t) {
                Logging.LOG.log(Level.WARNING, "Failed to get Minecraft news!", t);
            }
        });
    }

    public void refreshSkin(Account account) {
        Schedulers.androidUIThread().execute(() -> {
            if (skinLoader != null) {
                skinLoader.load(account, true);
            }
        });
    }
}
