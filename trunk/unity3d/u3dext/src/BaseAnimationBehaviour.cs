using System;
using UnityEngine;

namespace u3dext {
	
	/// <summary>
	/// Base behaviour that displaying animation.
	/// To use this script, do followoing first:
	/// 1. Inherit from this base class and attach to screen plane GameObject where to play the animation or other GameObject.
	/// 2. Provide animation textures and transparent texture.
	/// 3. Attach the screen plane GameObject 
	/// 4. Call PlayAnimation() in client with a callback(Playing completed) .
	/// 	Example:
	/// 	GameObject planeBoom = GameObject.Find ("plane_boom");
	///		BrickBoom animScript = (BrickBoom)planeBoom.GetComponent(typeof(BrickBoom));
	///		animScript.PlayAnimation(targetObject.transform.position, OnBrickBoomAnimationCompleted);
	/// </summary>
	public abstract class BaseAnimationBehaviour : MonoBehaviour {
		
		// === Public ===
		public GameObject playingScreen;
		public Texture[] animTextures;
		public Texture transparentTextrue;
		
		public bool isPlaying = false;
		public bool uninterruptedly = false;

		// === Private ===
		protected int animIdx;
//		protected Vector3 targetPosition;
		
		public delegate void AnimationDelegate(GameObject param);
		protected AnimationDelegate animationCallback;
		
		public BaseAnimationBehaviour () {

		}
		
		/// <summary>
		/// Start to play animation.
		/// </summary>
		public void PlayAnimation (AnimationDelegate animationCallback) {
			this.isPlaying = true;
			this.animationCallback = animationCallback;
		}

		
		protected virtual void Update () {
			if (isPlaying == true) {
				if (animIdx < animTextures.Length) {
					playingScreen.renderer.materials[0].SetTexture(
						"_MainTex",
						animTextures[animIdx++]
					);
				} else {

					if (uninterruptedly) {
						
					} else {
						// Hide screen after animation completed.
						playingScreen.renderer.materials[0].SetTexture(
						"_MainTex",
						transparentTextrue
						);
						animationCallback(playingScreen);
						isPlaying = false;
					}
					animIdx = 0;
				}
			}
		}
		
		
	}
}

