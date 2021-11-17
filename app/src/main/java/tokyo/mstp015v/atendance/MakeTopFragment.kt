package tokyo.mstp015v.atendance

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import tokyo.mstp015v.atendance.databinding.FragmentMakeTopBinding

class MakeTopFragment : Fragment() {
    private var _binding : FragmentMakeTopBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMakeTopBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //学生作成フラグメントへ移動
        binding.textGakuseiMove.setOnClickListener {
            val action = MakeTopFragmentDirections.actionMakeTopFragmentToGakuseiMakeKurasuListFragment()
            findNavController().navigate( action )
        }
        //クラス作成フラグメントへ移動
        binding.textKurasuMove.setOnClickListener{
            val action = MakeTopFragmentDirections.actionMakeTopFragmentToKurasuMakeFragment()
            findNavController().navigate( action )
        }
        //履修作成フラグメントへ移動
        binding.textRisyuuMove.setOnClickListener{
            val action = MakeTopFragmentDirections.actionMakeTopFragmentToRisyuuMakeFragment()
            findNavController().navigate( action )
        }
    }
}