package tokyo.mstp015v.atendance

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import tokyo.mstp015v.atendance.databinding.FragmentTimeTableBinding
import java.text.SimpleDateFormat
import java.util.*

class TimeTableFragment : Fragment() {


    private val args : TimeTableFragmentArgs by navArgs()
    private val res = listOf("月","火","水","木","金")
    private val weekday = mapOf(1 to "日",2 to "月",3 to "火",4 to "水",5 to "木" , 6 to "金" , 7 to "土")
    private var y =0
    private var m =0
    private var d = 0
    private var e = 0

    inner class PageAdapter( fa:FragmentActivity? ): FragmentStateAdapter(fa!!){
        override fun getItemCount():Int = res.size
        override fun createFragment(p:Int):Fragment = TimeTableContentsFragment.newInstance(res[p],args.kurasuMei,y,m,d)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    private var _binding : FragmentTimeTableBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentTimeTableBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewPager.adapter=PageAdapter( activity )

        //tablayoutの設定
        TabLayoutMediator(binding.tabLayout,binding.viewPager){tab,p->
            tab.text = res[p]
        }.attach()

        //日付を取得
        val calendar = Calendar.getInstance()
        y = calendar.get(Calendar.YEAR)
        m = calendar.get(Calendar.MONTH )
        d = calendar.get(Calendar.DAY_OF_MONTH)
        e = calendar.get(Calendar.DAY_OF_WEEK)
        binding.textDate.text = "${y}年${m+1}月${d}日(${weekday[e]}曜日)"

        //日付をタップした時のイベント
        binding.textDate.setOnClickListener{
            DateDialog(y,m,d){year,month,day,dayofweek->
                y = year
                m = month
                d = day
                e = dayofweek
                binding.textDate.text = "${y}年${m+1}月${d}日(${weekday[e]}曜日)"
                binding.tabLayout.getTabAt(e-2)?.select()
            }.show(parentFragmentManager,"date_dialog")
        }

        //ページャー切り替えのイベント
        //binding.viewPager.setPageTransformer(ZoomOutPageTransformer())
        binding.viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                //Log.d("pager",position.toString() )
                if( e-2 < position ){
                    val calendar = Calendar.getInstance()
                    calendar.set(y,m,d)
                    calendar.add(Calendar.DAY_OF_MONTH,position-(e-2))
                    y = calendar.get(Calendar.YEAR)
                    m = calendar.get(Calendar.MONTH)
                    d = calendar.get(Calendar.DAY_OF_MONTH)
                    e = calendar.get(Calendar.DAY_OF_WEEK)
                    binding.textDate.text = "${y}年${m+1}月${d}日(${weekday[e]}曜日)"
                }else if(e-2 > position ){
                    val calendar = Calendar.getInstance()
                    calendar.set(y,m,d)
                    calendar.add(Calendar.DAY_OF_MONTH,-((e-2)-position))
                    y = calendar.get(Calendar.YEAR)
                    m = calendar.get(Calendar.MONTH)
                    d = calendar.get(Calendar.DAY_OF_MONTH)
                    e = calendar.get(Calendar.DAY_OF_WEEK)
                    binding.textDate.text = "${y}年${m+1}月${d}日(${weekday[e]}曜日)"
                }
            }
        })
    }

    override fun onStart() {
        super.onStart()
        Log.d("youbi","${e-2}")

        //今日の日付にタブを切り替える
        //binding.tabLayout.getTabAt(e-2)?.select()
        binding.viewPager.setCurrentItem( e-2,false )
    }
}