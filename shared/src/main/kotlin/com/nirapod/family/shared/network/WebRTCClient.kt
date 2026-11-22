package com.nirapod.family.shared.network

import android.content.Context
import org.webrtc.*
import java.util.concurrent.Executors

class WebRTCClient(
    private val context: Context,
    private val signalingService: SignalingService,
    private val localDeviceId: String,
    private val targetDeviceId: String,
    private val isInitiator: Boolean,
    private val onRemoteStreamAdded: ((MediaStream) -> Unit)? = null
) {
    private val executor = Executors.newSingleThreadExecutor()
    private var peerConnectionFactory: PeerConnectionFactory? = null
    var peerConnection: PeerConnection? = null
    var localAudioTrack: AudioTrack? = null
    var localVideoTrack: VideoTrack? = null
    private var eglBase: EglBase = EglBase.create()

    val eglBaseContext: EglBase.Context
        get() = eglBase.eglBaseContext

    init {
        initializeFactory()
    }

    private fun initializeFactory() {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        val defaultVideoEncoderFactory = DefaultVideoEncoderFactory(
            eglBase.eglBaseContext, true, true
        )
        val defaultVideoDecoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(defaultVideoEncoderFactory)
            .setVideoDecoderFactory(defaultVideoDecoderFactory)
            .setOptions(PeerConnectionFactory.Options())
            .createPeerConnectionFactory()
    }

    fun startPeerConnection(streamType: String) {
        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
            PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer()
        )

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }

        peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {
                signalingService.sendCandidate(candidate, localDeviceId, targetDeviceId, streamType)
            }

            override fun onAddStream(stream: MediaStream) {
                onRemoteStreamAdded?.invoke(stream)
            }

            override fun onTrack(transceiver: RtpTransceiver?) {}
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
            override fun onSignalingChange(state: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}
            override fun onRemoveStream(stream: MediaStream?) {}
            override fun onDataChannel(dataChannel: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
        })

        listenForSignaling(streamType)

        if (isInitiator) {
            createOffer(streamType)
        }
    }

    private fun createOffer(streamType: String) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        peerConnection?.createOffer(object : SdpObserverAdapter() {
            override fun onCreateSuccess(desc: SessionDescription) {
                peerConnection?.setLocalDescription(SdpObserverAdapter(), desc)
                signalingService.sendOffer(desc.description, localDeviceId, targetDeviceId, streamType)
            }
        }, constraints)
    }

    fun onOfferReceived(sdp: String, streamType: String) {
        val desc = SessionDescription(SessionDescription.Type.OFFER, sdp)
        peerConnection?.setRemoteDescription(object : SdpObserverAdapter() {
            override fun onSetSuccess() {
                val constraints = MediaConstraints()
                peerConnection?.createAnswer(object : SdpObserverAdapter() {
                    override fun onCreateSuccess(answerDesc: SessionDescription) {
                        peerConnection?.setLocalDescription(SdpObserverAdapter(), answerDesc)
                        signalingService.sendAnswer(answerDesc.description, localDeviceId, targetDeviceId, streamType)
                    }
                }, constraints)
            }
        }, desc)
    }

    fun onAnswerReceived(sdp: String) {
        val desc = SessionDescription(SessionDescription.Type.ANSWER, sdp)
        peerConnection?.setRemoteDescription(SdpObserverAdapter(), desc)
    }

    fun onIceCandidateReceived(candidate: IceCandidate) {
        peerConnection?.addIceCandidate(candidate)
    }

    private fun listenForSignaling(streamType: String) {
        signalingService.listenForSignaling(localDeviceId) { msg ->
            when (msg.type) {
                "offer" -> msg.sdp?.let { onOfferReceived(it, streamType) }
                "answer" -> msg.sdp?.let { onAnswerReceived(it) }
                "ice_candidate" -> {
                    if (msg.candidate != null && msg.sdpMid != null) {
                        val candidate = IceCandidate(msg.sdpMid, msg.sdpMLineIndex, msg.candidate)
                        onIceCandidateReceived(candidate)
                    }
                }
            }
        }
    }

    fun createAudioTrack(audioSourceId: String = "ARDAMSa0"): AudioTrack? {
        val audioSource = peerConnectionFactory?.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory?.createAudioTrack(audioSourceId, audioSource)
        return localAudioTrack
    }

    fun createVideoTrack(videoSource: VideoSource, trackId: String = "ARDAMSv0"): VideoTrack? {
        localVideoTrack = peerConnectionFactory?.createVideoTrack(trackId, videoSource)
        return localVideoTrack
    }

    fun createVideoSource(isScreencast: Boolean): VideoSource? {
        return peerConnectionFactory?.createVideoSource(isScreencast)
    }

    fun close() {
        try {
            peerConnection?.close()
            peerConnection = null
            peerConnectionFactory?.dispose()
            peerConnectionFactory = null
            eglBase.release()
        } catch (_: Exception) {}
    }

    open class SdpObserverAdapter : SdpObserver {
        override fun onCreateSuccess(desc: SessionDescription) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(s: String?) {}
        override fun onSetFailure(s: String?) {}
    }
}
