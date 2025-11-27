package com.example.myimagetextclient;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

public class HomeFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private LinearLayout emptyLayout;
    private View retryButton;

    private PostAdapter adapter;
    private StaggeredGridLayoutManager layoutManager;

    private boolean isLoadingMore = false;

    private TextView tabCommunity, tabBeijing, tabGroupBuy, tabFollow, tabRecommend;
    private ImageView iconSearch;

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initTopTabs(view);  // 顶部标签栏
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefresh   = view.findViewById(R.id.swipe_refresh);
        recyclerView   = view.findViewById(R.id.recycler_view);
        emptyLayout    = view.findViewById(R.id.empty_layout);
        retryButton    = view.findViewById(R.id.btn_retry);

        layoutManager = new StaggeredGridLayoutManager(
                2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        /** 下拉刷新 */
        swipeRefresh.setOnRefreshListener(this::loadInitialData);

        /** 重试按钮 */
        retryButton.setOnClickListener(v -> {
            emptyLayout.setVisibility(View.GONE);
            swipeRefresh.setVisibility(View.VISIBLE);
            swipeRefresh.setRefreshing(true);
            loadInitialData();
        });

        /** 启动自动刷新 */
        swipeRefresh.setRefreshing(true);
        loadInitialData();
    }

    /** =========================
     *      1. 首页加载 API 数据
     * =========================*/
    private void loadInitialData() {

        NetworkManager.getFeed(20, false, new NetworkManager.Callback() {
            @Override
            public void onSuccess(String json) {

                PostResponse response = JsonHelper.toObject(json, PostResponse.class);

                if (response == null || response.status_code != 0) {
                    onLoadFailed();
                    return;
                }

                List<Post> posts = PostData.convertFromApi(response.post_list);
                PostData.currentPosts = posts;
                PostData.hasMore = (response.has_more == 1);

                requireActivity().runOnUiThread(() -> {

                    swipeRefresh.setRefreshing(false);
                    emptyLayout.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    if (adapter == null) {
                        adapter = new PostAdapter(getContext(), posts);
                        recyclerView.setAdapter(adapter);

                        /** 添加滚动监听 → 上滑自动加载更多 */
                        addScrollLoadMoreListener();
                    } else {
                        adapter.setPosts(posts);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                onLoadFailed();
            }
        });
    }

    private void onLoadFailed() {
        requireActivity().runOnUiThread(() -> {
            swipeRefresh.setRefreshing(false);
            recyclerView.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
        });
    }

    /** =========================
     *      2. 加载更多（API）
     * =========================*/
    private void addScrollLoadMoreListener() {

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView,
                                   int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy <= 0) return;      // 只在向下滚动时触发
                if (isLoadingMore) return;
                if (!PostData.hasMore) return;

                int[] lastVisible = layoutManager.findLastVisibleItemPositions(null);
                int lastPos = Math.max(lastVisible[0], lastVisible[1]);

                if (adapter != null && lastPos >= adapter.getItemCount() - 1) {
                    loadMoreData();
                }
            }
        });
    }

    private void loadMoreData() {

        isLoadingMore = true;
        adapter.addLoadingFooter();

        PostData.loadMore(getContext(), new PostData.LoadCallback() {
            @Override
            public void onLoaded(List<Post> newPosts) {

                requireActivity().runOnUiThread(() -> {

                    adapter.removeLoadingFooter();

                    if (newPosts.isEmpty()) {
                        Toast.makeText(getContext(), "没有更多内容了", Toast.LENGTH_SHORT).show();
                    } else {
                        adapter.addPosts(newPosts);
                    }

                    isLoadingMore = false;
                });
            }

            @Override
            public void onFailed(String msg) {

                requireActivity().runOnUiThread(() -> {
                    adapter.removeLoadingFooter();
                    Toast.makeText(getContext(), "加载更多失败", Toast.LENGTH_SHORT).show();
                    isLoadingMore = false;
                });
            }
        });
    }

    /** =========================
     *      顶部 Tab 栏
     * =========================*/
    private void initTopTabs(View view) {
        tabBeijing   = view.findViewById(R.id.tab_beijing);
        tabGroupBuy  = view.findViewById(R.id.tab_groupbuy);
        tabFollow    = view.findViewById(R.id.tab_follow);
        tabCommunity = view.findViewById(R.id.tab_community);
        tabRecommend = view.findViewById(R.id.tab_recommend);
        iconSearch   = view.findViewById(R.id.icon_search);

        highlightTab(tabCommunity);

        tabBeijing.setEnabled(false);
        tabGroupBuy.setEnabled(false);
        tabFollow.setEnabled(false);
        tabRecommend.setEnabled(false);
        iconSearch.setEnabled(false);
    }

    private void highlightTab(TextView tab) {
        tab.setTextColor(Color.BLACK);
        tab.setTextSize(18);
        tab.setTypeface(Typeface.DEFAULT_BOLD);
    }
}
