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
		
		public delegate void AnimationDelegate (GameObject param);

		// === Public ===
//		public GameObject playingScreen;

		public string animTexPrefix;
		public int animTexSize;

		public Texture transparentTextrue;
		public bool isPlaying = false;
		public bool uninterruptedly = false;

		// === Private ===
		protected int animIdx;
		protected AnimationDelegate animationCallback;
		private Texture[] animTextures;

		public BaseAnimationBehaviour () {
		}
		
		/// <summary>
		/// Start to play animation.
		/// </summary>
		public void PlayAnimation (AnimationDelegate animationCallback) {
			this.isPlaying = true;
			this.animationCallback = animationCallback;
			if(animTextures==null || animTextures.Length == 0) {
				animTextures = new Texture[animTexSize];
				for(int i = 0 ; i<animTexSize ; i++){
					string suffix = (i<10 ? ("0" + i) : ("" + i));
					animTextures[i] = (Texture)Resources.Load(animTexPrefix + suffix);
				}
			}
		}
		
		protected virtual void Update () {
			if (isPlaying == true) {
				if (animIdx < animTextures.Length) {
					gameObject.renderer.materials[0].SetTexture(
						"_MainTex",
						animTextures[animIdx++]
					);
				} else {

					if (uninterruptedly) {
						
					} else {
						// Hide screen after animation completed.
						gameObject.renderer.materials[0].SetTexture(
							"_MainTex",
							transparentTextrue
						);
						animationCallback(gameObject);
						isPlaying = false;
					}
					animIdx = 0;
				}
			}
		}
		
		
	}
}

