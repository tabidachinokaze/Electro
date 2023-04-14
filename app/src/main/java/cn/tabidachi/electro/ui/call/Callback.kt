package cn.tabidachi.electro.ui.call

import android.util.Log
import kotlinx.serialization.Serializable
import org.webrtc.CandidatePairChangeEvent
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

@Serializable
data class RemoteSessionDescription(
    val type: SessionDescription.Type,
    val description: String
) {
    constructor(sessionDescription: SessionDescription) : this(sessionDescription.type, sessionDescription.description)
}

fun RemoteSessionDescription.toLocal() = SessionDescription(this.type, this.description)

open class PeerConnectionObserver(tag: String = "") : PeerConnection.Observer {
    private val TAG = "$tag PeerConnectionObserver"
    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
        Log.d(TAG, "onSignalingChange: $p0")
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
        Log.d(TAG, "onIceConnectionChange: $p0")
    }

    override fun onStandardizedIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
        Log.d(TAG, "onStandardizedIceConnectionChange: $newState")
    }

    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
        Log.d(TAG, "onConnectionChange: $newState")
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        Log.d(TAG, "onIceConnectionReceivingChange: $p0")
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        Log.d(TAG, "onIceGatheringChange: $p0")
    }

    override fun onIceCandidate(candidate: IceCandidate?) {
        Log.d(TAG, "onIceCandidate: $candidate")
    }

    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
        Log.d(TAG, "onIceCandidatesRemoved: $p0")
    }

    override fun onSelectedCandidatePairChanged(event: CandidatePairChangeEvent?) {
        Log.d(TAG, "onSelectedCandidatePairChanged: $event")
    }

    override fun onAddStream(p0: MediaStream?) {
        Log.d(TAG, "onAddStream: $p0")
    }

    override fun onRemoveStream(p0: MediaStream?) {
        Log.d(TAG, "onRemoveStream: $p0")
    }

    override fun onDataChannel(p0: DataChannel?) {
        Log.d(TAG, "onDataChannel: $p0")
    }

    override fun onRenegotiationNeeded() {
        Log.d(TAG, "onRenegotiationNeeded: ")
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        Log.d(TAG, "onAddTrack: $p0 $p1")
    }

    override fun onTrack(transceiver: RtpTransceiver?) {
        Log.d(TAG, "onTrack: $transceiver")
    }
}

open class SessionDescriptionObserver(
    private val tag: String = ""
) : SdpObserver {
    private val TAG = "$tag SdpObserver"
    override fun onCreateSuccess(sdp: SessionDescription?) {
        Log.d(TAG, "onCreateSuccess: ${sdp?.type} ${sdp?.description}")
    }

    override fun onSetSuccess() {
        Log.d(TAG, "onSetSuccess: ")
    }

    override fun onCreateFailure(p0: String?) {
        Log.d(TAG, "onCreateFailure: $p0")
    }

    override fun onSetFailure(p0: String?) {
        Log.d(TAG, "onSetFailure: $p0")
    }
}