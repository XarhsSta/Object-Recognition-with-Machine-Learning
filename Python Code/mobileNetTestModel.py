import numpy as np
import tensorflow as tf
import tensorflow.keras as keras
from tensorflow.keras.applications.mobilenet import preprocess_input
from tensorflow.keras.models import Model, load_model
from tensorflow.keras import optimizers
from tensorflow.keras.datasets import cifar100
from keras.utils import np_utils
import time
from sklearn.metrics import mean_absolute_error, mean_squared_error, coverage_error, \
    label_ranking_average_precision_score, label_ranking_loss
from sklearn.metrics import precision_score,accuracy_score, recall_score, f1_score

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

saved_model_dir = 'saved_model_3'
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

y_test_predictions = np.argmax(y_test_predictions, axis=1)
y_test = np.argmax(y_test, axis=1)

precision = precision_score(y_test, y_test_predictions, average='macro')
accuracy = accuracy_score(y_test, y_test_predictions)
recall = recall_score(y_test, y_test_predictions, average='macro')
f1 = f1_score(y_test, y_test_predictions, average='macro')

print("Mean Absolute Error: ", meanAbsoluteError)
print("Mean Squared Error: ", meanSquaredError)
print("Coverage Error: ", coverageError)
print("Label Ranking Average Precision Score: ", labelRankingAverage)
print("Label Ranking Loss: ", labelRankingLoss)
print("Precision: ", precision)
print("Accuracy: ", accuracy)
print("Recall: ", recall)
print("F1: ", f1)

#Save to .txt
file = open("mobileNetScores.txt", "a")
file.write("===========================================\n")
file.write("New Test\n")
file.write("===========================================\n")
file.write("Multilabel Ranking Tests\n\n")
file.write("Mean Absolute Error: %2.5f\n" % meanAbsoluteError)
file.write("Mean Squared Error: %2.5f\n" % meanSquaredError)
file.write("Coverage Error: %2.5f\n" % coverageError)
file.write("Label Ranking Average Precision Score: %2.5f\n" % labelRankingAverage)
file.write("Label Ranking Loss: %2.5f\n\n" % labelRankingLoss)
file.write("Classification Metrics\n\n")
file.write("Precision: %2.5f\n" % precision)
file.write("Accuracy: %2.5f\n" % accuracy)
file.write("Recall: %2.5f\n" % recall)
file.write("F1: %2.5f\n\n" % f1)

file.close()
# Convert the model
converter = tf.lite.TFLiteConverter.from_saved_model(saved_model_dir) # path to the SavedModel directory
tflite_model = converter.convert()

with open('mobileNetModel.tflite', 'wb') as f:
    f.write(tflite_model)