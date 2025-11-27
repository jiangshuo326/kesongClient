package com.example.myimagetextclient;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private boolean firstLoadAttempt = true;
    private boolean isLoadingMore = false;
    private boolean noMoreData = false;
    private boolean userRefresh = false;
    private final int LOAD_DELAY = 1000; // 模拟网络请求延迟

    private TextView tabCommunity, tabBeijing, tabGroupBuy, tabFollow, tabRecommend;
    private ImageView iconSearch;

    public HomeFragment() {
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // 加载 fragment_home.xml
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 初始化顶部 TabBar
        initTopTabs(view);

        // 不要再调用 initRecyclerView(view)，因为 onViewCreated 里已经做了
        return view;
    }
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//        // 加载社区首页布局（fragment_home.xml）
//        return inflater.inflate(R.layout.fragment_home, container, false);
//    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        recyclerView = view.findViewById(R.id.recycler_view);
        emptyLayout = view.findViewById(R.id.empty_layout);
        retryButton = view.findViewById(R.id.btn_retry);

        // 初始化 RecyclerView 布局管理器（双列瀑布流）
        layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        // 下拉刷新监听
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 用户手动下拉刷新
                userRefresh = true;
                // 延迟模拟刷新请求
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadData();
                    }
                }, LOAD_DELAY);
            }
        });

        // 重试按钮点击监听
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击重试：隐藏空态布局，显示加载进度，然后重新加载数据
                emptyLayout.setVisibility(View.GONE);
                swipeRefresh.setVisibility(View.VISIBLE);
                swipeRefresh.setRefreshing(true);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadData();
                    }
                }, LOAD_DELAY);
            }
        });

        // 首次进入：显示刷新动画，开始加载数据
        swipeRefresh.setRefreshing(true);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        }, LOAD_DELAY);
    }

    // 加载数据（首次加载或下拉刷新）
    private void loadData() {
        Context context = getContext();
        if (context == null) return;
        if (firstLoadAttempt) {
            // 首次加载模拟失败，显示空态页面
            firstLoadAttempt = false;
            swipeRefresh.setRefreshing(false);
            recyclerView.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
        } else {
            // 加载成功：获取作品列表数据并显示
            List<Post> posts = PostData.getInitialPosts(context);
            PostData.currentPosts = posts; // 记录当前列表数据
            swipeRefresh.setRefreshing(false);
            emptyLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            noMoreData = false;
            // 初始化或更新Adapter数据
            if (adapter == null) {
                adapter = new PostAdapter(context, posts);
                recyclerView.setAdapter(adapter);
                // 添加滚动监听，用于检测上滑加载更多
                recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        // 向下滚动且未在加载状态，检查是否到达底部触发加载更多
                        if (dy > 0 && !isLoadingMore && !noMoreData) {
                            int[] lastVisiblePositions = layoutManager.findLastVisibleItemPositions(null);
                            int lastPos = 0;
                            for (int pos : lastVisiblePositions) {
                                if (pos > lastPos) lastPos = pos;
                            }
                            if (lastPos >= adapter.getItemCount() - 1) {
                                // 滑动到列表底部，开始加载更多
                                loadMoreData();
                            }
                        }
                    }
                });
            } else {
                // 刷新已有列表数据
                adapter.setPosts(posts);
            }
            if (userRefresh) {
                // 如果是用户手动刷新，弹出“刷新完成”提示
                Toast.makeText(context, "刷新完成", Toast.LENGTH_SHORT).show();
                userRefresh = false;
            }
        }
    }

    // 加载更多（分页加载）
    private void loadMoreData() {
        isLoadingMore = true;
        // 列表底部添加加载中提示
        adapter.addLoadingFooter();
        // 延迟模拟网络获取更多数据
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Context context = getContext();
                if (context == null) return;
                List<Post> newPosts = PostData.getMorePosts(context);
                // 移除加载中提示
                adapter.removeLoadingFooter();
                if (newPosts.isEmpty()) {
                    // 没有更多数据了
                    noMoreData = true;
                    Toast.makeText(context, "没有更多内容了", Toast.LENGTH_SHORT).show();
                } else {
                    // 将新数据追加到列表
                    adapter.addPosts(newPosts);
                    PostData.currentPosts.addAll(newPosts);
                }
                isLoadingMore = false;
            }
        }, LOAD_DELAY);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 返回页面时刷新列表，以更新点赞状态变化
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void initTopTabs(View view) {
        tabBeijing = view.findViewById(R.id.tab_beijing);
        tabGroupBuy = view.findViewById(R.id.tab_groupbuy);
        tabFollow = view.findViewById(R.id.tab_follow);
        tabCommunity = view.findViewById(R.id.tab_community);
        tabRecommend = view.findViewById(R.id.tab_recommend);
        iconSearch = view.findViewById(R.id.icon_search);

        // 默认选中社区（高亮）
        highlightTab(tabCommunity);

        // 其余全部禁止点击
        tabBeijing.setEnabled(false);
        tabGroupBuy.setEnabled(false);
        tabFollow.setEnabled(false);
        tabRecommend.setEnabled(false);
        iconSearch.setEnabled(false);
    }

    private void highlightTab(TextView selected) {
        // 设置高亮
        selected.setTextColor(Color.BLACK);
        selected.setTextSize(18);

        // 下划线（可选）
        selected.setTypeface(Typeface.DEFAULT_BOLD);
    }
}
