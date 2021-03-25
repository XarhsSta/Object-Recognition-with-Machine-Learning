import numpy as np
import time
import tensorflow as tf
import tensorflow.keras as keras
from tensorflow.keras.applications.efficientnet import EfficientNetB0, preprocess_input
from tensorflow.keras.models import Model, Sequential, save_model
from tensorflow.keras.layers import Input, UpSampling2D, Flatten, BatchNormalization, Dense, Dropout, \
    GlobalAveragePooling2D
from tensorflow.keras import optimizers
from tensorflow.keras.datasets import cifar100
from keras.utils import np_utils
from tensorflow.keras.callbacks import ModelCheckpoint
from os.path import dirname

num_classes = 100
nb_epochs = 15
batch_size = 32

config = tf.compat.v1.ConfigProto(gpu_options=tf.compat.v1.GPUOptions(per_process_gpu_memory_fraction=0.6))
config.gpu_options.allow_growth = True
session = tf.compat.v1.Session(config=config)
tf.compat.v1.keras.backend.set_session(session)

(x_train, y_train), (x_test, y_test) = cifar100.load_data()

x_train = preprocess_input(x_train)
x_test = preprocess_input(x_test)

y_train = np_utils.to_categorical(y_train, num_classes)
y_test = np_utils.to_categorical(y_test, num_classes)

efficientNetModel = EfficientNetB0(include_top=False, weights='imagenet', input_shape=(224, 224, 3))


for layer in efficientNetModel.layers:
    if isinstance(layer, BatchNormalization):
        layer.trainable = True
    else:
        layer.trainable = False

model = Sequential()
model.add(keras.Input(shape=(32, 32, 3)))
model.add(UpSampling2D())
model.add(UpSampling2D())
model.add(UpSampling2D())
model.add(keras.layers.Cropping2D(16))
model.add(efficientNetModel)
model.add(Dense(256, activation='relu'))
model.add(GlobalAveragePooling2D())
model.add(Dropout(.25))
model.add(BatchNormalization())
model.add(Dense(num_classes, activation='softmax'))

model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])

checkpoint_path = "training_2/cp-{epoch:04d}.ckpt"
checkpoint_dir = dirname(checkpoint_path)

cp_callback = ModelCheckpoint(filepath=checkpoint_path,
                              save_weights_only=True,
                              save_freq=3 * 781,
                              verbose=1)
model.save_weights(checkpoint_path.format(epoch=0))
t = time.time()
historytemp = model.fit(x_train, y_train, batch_size,
                        steps_per_epoch=x_train.shape[0] // 64,
                        epochs=nb_epochs,
                        validation_data=(x_test, y_test),
                        callbacks=[cp_callback])

print('Training time: %s' % (time.time() - t))

model.summary()
model.save('E:\\Projects\\saved_model_2')
