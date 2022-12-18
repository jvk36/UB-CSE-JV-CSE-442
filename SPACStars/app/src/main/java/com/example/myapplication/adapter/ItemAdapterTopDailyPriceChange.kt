package com.example.myapplication.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.PriceFunctions
import com.example.myapplication.R
import com.example.myapplication.model.SPACTopDailyPriceChange

class ItemAdapterTopDailyPriceChange(
        private val context: Context,
        private val dataset: List<SPACTopDailyPriceChange>,
        private val preloidata: MutableList<Array<String>> = PriceFunctions.getdata("Pre+LOI", context),
        private val definitiveagreementdata: MutableList<Array<String>> = PriceFunctions.getdata("Definitive+Agreement", context)
) : RecyclerView.Adapter<ItemAdapterTopDailyPriceChange.ItemViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just an SPACLivePrices object.
    class ItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val textView1: TextView = view.findViewById(R.id.item_title1)
        val textView2: TextView = view.findViewById(R.id.item_title2)
        val textView3: TextView = view.findViewById(R.id.item_title3)
        val textView4: TextView = view.findViewById(R.id.item_title4)
    }

    /**
     * Create new views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        // create a new view
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_top_daily_spac, parent, false)

        return ItemViewHolder(adapterLayout)
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = dataset[position]
        holder.textView1.text = item.stringResourceId1
        holder.textView2.text = item.stringResourceId2
        holder.textView3.text = item.stringResourceId3.toString().plus("%")
        holder.textView4.text = item.stringResourceId4

        //Determine whether it belongs to PreLoi or DefAgreement. If not, list[0] = SPAC NOT FOUND
        val thisdatapreloi = PriceFunctions.getSPACdata(preloidata, holder.textView1.text.toString())
        val thisdatadefagreement = PriceFunctions.getSPACdata(definitiveagreementdata, holder.textView1.text.toString())
        if (thisdatapreloi[0] != "SPAC NOT FOUND") {
            PriceFunctions.onclicksetter_topdaily(holder, "Pre LOI", thisdatapreloi, context)
        } else if (thisdatadefagreement[0] != "SPAC NOT FOUND") {
            PriceFunctions.onclicksetter_topdaily(holder, "Definitive Agreement", thisdatadefagreement, context)
        } else {
            PriceFunctions.onclicksetter_topdaily(holder, "NOT_FOUND", arrayOf(item.stringResourceId1, item.stringResourceId3.toString().plus("%"), item.stringResourceId2), context)
        }

    }

    /**
     * Return the size of your dataset (invoked by the layout manager)
     */
    override fun getItemCount() = dataset.size
}