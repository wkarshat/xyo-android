package network.xyo.client;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import network.xyo.sdk.data.Entry;
import network.xyo.sdk.nodes.Node;

public class LedgerActivity extends AppCompatActivity {

    private Node node;

    private View recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ledger_activity);

        node = Node.get(getIntent().getStringExtra(NodeDetailFragment.NODE_NAME));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        recyclerView = findViewById(R.id.entry_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {

        final LedgerActivity.SimpleItemRecyclerViewAdapter adapter = new LedgerActivity.SimpleItemRecyclerViewAdapter(this, node.ledger, false);

        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);

        node.setListener(new Node.Listener() {
            @Override
            public void in(Node node, byte[] bytes) {

            }

            @Override
            public void out(Node node, byte[] bytes) {

            }

            @Override
            public void updated() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<LedgerActivity.SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final LedgerActivity mParentActivity;
        private final List<Entry> mValues;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Entry entry = (Entry) view.getTag();
                {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, EntryDetailActivity.class);
                    intent.putExtra(NodeDetailFragment.NODE_NAME, node.getName());
                    intent.putExtra(EntryDetailActivity.ENTRY_HASH, entry.getId());

                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(LedgerActivity parent,
                                      List<Entry> items,
                                      boolean twoPane) {
            mValues = items;
            mParentActivity = parent;
        }

        @Override
        public LedgerActivity.SimpleItemRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.entry_list_content, parent, false);
            return new LedgerActivity.SimpleItemRecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final LedgerActivity.SimpleItemRecyclerViewAdapter.ViewHolder holder, int position) {

            Entry entry = mValues.get(position);

            holder.type.setText(entry.getTypeString());
            holder.epoch.setText(entry.getEpochString());

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView type;
            final TextView epoch;

            ViewHolder(View view) {
                super(view);
                type = (TextView) view.findViewById(R.id.type);
                epoch = (TextView) view.findViewById(R.id.epoch);
            }
        }
    }

}
