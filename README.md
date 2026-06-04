# Maize Disease Detection: A Case Study in Deep Learning for Sustainable Agriculture

## Authors
- **Joshua Kyalo** (COM/4132/20)
- **Timothy Ng’uono** (COM/1387/20)
- **Supervisor:** Dr. Gibson Kimutai, Moi University

---

## 1. Executive Summary
This project demonstrates the application of Machine Learning and Computer Vision to the early detection of maize diseases. By utilizing Convolutional Neural Networks (CNN), we developed a model capable of identifying healthy maize plants versus those afflicted by **Cercospora**, **Common Rust**, and **Northern Leaf Blight**. The model achieved an accuracy of **88%** and was integrated into a native mobile application for real-time agricultural diagnosis.

## 2. Problem Statement
Maize is a critical food crop globally, but its yields are often threatened by diseases that are difficult for non-experts to identify early. Manual diagnosis is time-consuming, costly, and prone to error, leading to delayed interventions and significant crop loss.

## 3. Proposed Solution
An automated disease detection system consisting of:
- A deep learning model (CNN) trained on labeled leaf images.
- A mobile application built with **Jetpack Compose** that allows farmers to capture images and receive instant diagnostic results.

## 4. System Architecture
The system follows a Research -> Strategy -> Execution workflow, where the model is trained in a cloud/desktop environment and deployed to mobile via **TensorFlow Lite**.

### Software Stack
- **Modeling:** Python, TensorFlow, Keras
- **Mobile Development:** Kotlin, Jetpack Compose, Material3
- **Deployment:** TensorFlow Lite (TFLite)
- **Image Processing:** Coil, Android Support Libraries

## 5. Machine Learning Implementation

### Data Preprocessing & Augmentation
Images were resized to **150x150** pixels and normalized to a range of [0, 1]. Data augmentation techniques (rotations, flips, zooms) were used to improve model generalization and prevent overfitting.

### Model Architecture
The CNN architecture consists of seven convolutional layers followed by max-pooling and batch normalization, feeding into a dense neural network for final classification.
```python
# Simplified Architecture Snippet
model.add(Conv2D(32, (3,3), padding='same', activation='relu', input_shape=(150,150,3)))
model.add(MaxPooling2D(2,2))
# ... (6 more convolutional blocks)
model.add(Flatten())
model.add(Dense(128, activation='relu'))
model.add(Dense(4, activation='softmax')) # 4 Classes: Cercospora, Rust, Blight, Healthy
```

## 6. Mobile Application Features
The mobile app provides a streamlined user interface for agricultural monitoring:
- **Real-time Inference:** Process images captured directly from the camera or selected from the gallery.
- **Probabilistic Results:** Displays the top prediction along with its confidence percentage, plus alternative possibilities sorted by probability.
- **Offline Capability:** The TFLite model runs locally on the device, requiring no internet connection for inference.

## 7. Performance & Reliability
- **Accuracy:** 88% on validation sets.
- **Efficiency:** Optimized model size for mobile deployment without compromising diagnostic speed.
- **Reliability:** Built-in error handling for low-quality images and hardware constraints.

## 8. Conclusion
The integration of deep learning into mobile platforms offers a cost-effective and efficient tool for sustainable maize farming. By enabling early detection, this application empowers farmers to mitigate yield losses and contribute to global food security.

---

## 9. References
1. Hope, T., Resheff, Y. (2017). *Learning TensorFlow: A Guide to Building Deep Learning Systems*.
2. Li, Y. et al. (2022). *One-Stage Disease Detection Method for Maize Leaf Based on Multi-Scale Feature Fusion*. IEEE.
3. Girshick et al. (2014). *Region-based Convolution Neural Network (R-CNN)*.
4. Redmon et al. (2016). *You Only Look Once (YOLO) Method*.

---

## Project Structure
- `/CNN`: Contains the Python training scripts and the Keras model.
- `/mobile-app`: The complete Android Studio project.
- `/mobile-app/app/src/main/ml`: The compiled `.tflite` model used by the app.
