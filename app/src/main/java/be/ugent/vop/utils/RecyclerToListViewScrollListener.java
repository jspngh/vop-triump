package be.ugent.vop.utils;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.AbsListView;

public class RecyclerToListViewScrollListener extends RecyclerView.OnScrollListener {
    private final AbsListView.OnScrollListener scrollListener;
    private int lastFirstVisible = -1;
    private int lastVisibleCount = -1;
    private int lastItemCount = -1;

    public RecyclerToListViewScrollListener(AbsListView.OnScrollListener scrollListener) {
        this.scrollListener = scrollListener;
    }

    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int firstVisible = layoutManager.findFirstVisibleItemPosition();
        int visibleCount = Math.abs(firstVisible - layoutManager.findLastVisibleItemPosition());
        int itemCount = recyclerView.getAdapter().getItemCount();
        if (firstVisible != lastFirstVisible || visibleCount != lastVisibleCount
                || itemCount != lastItemCount) {
            scrollListener.onScroll(null, firstVisible, visibleCount, itemCount);
            lastFirstVisible = firstVisible;
            lastFirstVisible = visibleCount;
            lastItemCount = itemCount;
        }
    }
}