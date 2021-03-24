import numpy as np
import tensorflow as tf
import tensorflow.keras as keras
from tensorflow.keras.applications.resnet50 import preprocess_input
from tensorflow.keras.models import Model, load_model
from tensorflow.keras import optimizers
from tensorflow.keras.datasets import cifar100
from keras.utils import np_utils
import time
from sklearn.metrics import mean_absolute_error, mean_squared_error, coverage_error, \
    label_ranking_average_precision_score, label_ranking_loss

num_classes = 100
nb_epochs = 10

config = tf.compat.v1.ConfigProto(gpu_options=tf.compat.v1.GPUOptions(per_process_gpu_memory_fraction=0.6))
config.gpu_options.allow_growth = True
session = tf.compat.v1.Session(config=config)
tf.compat.v1.keras.backend.set_session(session)

(x_train, y_train), (x_test, y_test) = cifar100.load_data()

x_train = preprocess_input(x_train)
x_test = preprocess_input(x_test)
y_train = np_utils.to_categorical(y_train, num_classes)
y_test = np_utils.to_categorical(y_test, num_classes)

saved_model_dir = 'saved_model'
loaded_model = load_model(saved_model_dir)

loaded_model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])
loaded_model.summary()

loss, acc = loaded_model.evaluate(x_test, y_test, verbose=2)
print('Restored model, accuracy: {:5.2f}%'.format(100 * acc))
print('Restored model, loss: {:5.2f}%'.format(100 * loss))

y_test_predictions = loaded_model.predict(x_test)

meanAbsoluteError = mean_absolute_error(y_test, y_test_predictions)
meanSquaredError = mean_squared_error(y_test, y_test_predictions)
coverageError = coverage_error(y_test, y_test_predictions)
labelRankingAverage = label_ranking_average_precision_score(y_test, y_test_predictions)
labelRankingLoss = label_ranking_loss(y_test, y_test_predictions)

print("Mean Absolute Error: ", meanAbsoluteError)
print("Mean Squared Error: ", meanSquaredError)
print("Coverage Error: ", coverageError)
print("Label Ranking Average Precision Score: ", labelRankingAverage)
print("Label Ranking Loss: ", labelRankingLoss)

# Convert the model
converter = tf.lite.TFLiteConverter.from_saved_model(saved_model_dir) # path to the SavedModel directory
tflite_model = converter.convert()

with open('resnetModel.tflite', 'wb') as f:
    f.write(tflite_model)