package tokyo.mstp015v.atendance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class SheetIdRealmAdapter (data: OrderedRealmCollection<SheetId>) :
    RealmRecyclerViewAdapter<SheetId, SheetIdRealmAdapter.ViewHolder>(data,true) {

        private var listener: ((String?) -> Unit)? = null

        fun setOnItemClickListener( listener : (String?) -> Unit ){
            this.listener = listener
        }

        class ViewHolder( cell: View) : RecyclerView.ViewHolder( cell ){
            val textId : TextView = cell.findViewById(R.id.textSheetId_Id)
            val textSheetId : TextView = cell.findViewById(R.id.textSheetId_SheetId)
            val textYmd:TextView = cell.findViewById(R.id.textSheetId_ymd)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.sheetid_item,parent,false)
            return ViewHolder( view )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val sheetid:SheetId?=getItem(position)
            holder.textId.text = sheetid!!.id.toString()
            holder.textSheetId.text = sheetid!!.sheetId.substring(0,5) + "..."
            holder.textYmd.text = sheetid!!.ymd
            holder.itemView.setOnClickListener{
                listener?.invoke(sheetid!!.sheetId)
            }
        }

        override fun getItemId(position: Int): Long {
            return super.getItemId(position)
        }
}