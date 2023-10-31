import random
import numpy as np
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras.optimizers import Adam
from tensorflow.keras import layers
import matplotlib.pyplot as plt
import math
import numpy as np
from scipy.io import wavfile

import pandas as pd
from tensorflow.keras.regularizers import l2
from sklearn.utils import shuffle
import json

import os, sys, matplotlib.pyplot as plt, numpy as np, itertools, math
from random import seed
from random import random, randint
from scipy.spatial import distance
from  tqdm import tqdm
import random
import warnings
import pickle
import pandas as pd
from math import factorial
from sklearn import preprocessing

import os
import librosa
X = []
y = []

def euclidean_distance(vects):
    x, y = vects
    sum_square = tf.math.reduce_sum(tf.math.square(x - y), axis=1, keepdims=True)
    return tf.math.sqrt(tf.math.maximum(sum_square, tf.keras.backend.epsilon()))

def loss(margin=1):

    # Contrastive loss = mean( (1-true_value) * square(prediction) +
    #                         true_value * square( max(margin-prediction, 0) ))
    def contrastive_loss(y_true, y_pred):

        square_pred = tf.math.square(y_pred)
        margin_square = tf.math.square(tf.math.maximum(margin - (y_pred), 0))
        return tf.math.reduce_mean(
            (1-y_true) * square_pred + (y_true) * margin_square)

    return contrastive_loss

def plt_metric(history, metric, title, has_valid=True):
    plt.plot(history[metric])
    if has_valid:
        plt.plot(history["val_" + metric])
        plt.legend(["train", "validation"], loc="upper left")
    plt.title(title)
    plt.ylabel(metric)
    plt.xlabel("epoch")
    plt.savefig(metric+"newdatatest.png")
    plt.show()

def single_model(X1, X2, y,test_X1,test_X2,test_y) :


    input = layers.Input((20, 657, 1))
#    x = tf.keras.layers.BatchNormalization()(input)
    x = layers.Conv2D(filters=32, kernel_size=(3,3), strides=(2,2),activation="relu", padding="same")(input)

#    x = layers.BatchNormalization()(x)
    x = layers.MaxPool2D(pool_size=(2,2), strides=(1,1), padding="same")(x)

    x = layers.Conv2D(filters=32, kernel_size=(2,2), strides=(1,1), activation="relu", padding="same")(x)

#    x = layers.BatchNormalization()(x)
    x = layers.MaxPool2D(pool_size=(2,2), strides=(1,1), padding="same")(x)


    x = layers.Conv2D(filters=64, kernel_size=(2,2), strides=(1,1),activation="relu", padding="same")(x)

#    x = layers.BatchNormalization()(x)
    x = layers.MaxPool2D(pool_size=(2,2), strides=(1,1), padding="same")(x)


#     x = layers.Conv2D(filters=128, kernel_size=(2,2), strides=(1,1),activation="relu", padding="same")(x)

# #    x = layers.BatchNormalization()(x)
#     x = layers.MaxPool2D(pool_size=(2,2), strides=(1,1), padding="same")(x)
# #
#     x = layers.Conv2D(filters=256, kernel_size=(2,2), strides=(1,1),activation="relu")(x)

#   #    x = layers.BatchNormalization()(x)
#     x = layers.MaxPool2D(pool_size=(2,2), strides=(1,1), padding="same")(x)

    x = layers.Conv2D(filters=64, kernel_size=(2,2), strides=(1,1),activation="relu", padding="same")(x)

#    x = layers.BatchNormalization()(x)
    x = layers.MaxPool2D(pool_size=(2,2), strides=(1,1), padding="same")(x)
#
  #   x = layers.Conv2D(filters=256, kernel_size=(2,2), strides=(1,1),activation="relu")(x)

  # #    x = layers.BatchNormalization()(x)
  #   x = layers.MaxPool2D(pool_size=(2,2), strides=(1,1), padding="same")(x)


#    x = layers.Conv2D(filters=512, kernel_size=(2,2), strides=(1,1),activation="relu", padding="valid")(x)
#
#    x = layers.BatchNormalization()(x)
#    x = layers.MaxPool2D(pool_size=(2,2), strides=(1,1), padding="valid")(x)

    x = layers.Flatten()(x)

#    x=layers.Dense(128, activation="relu")(x)
##    x=layers.Dropout(0.2)(x)
#    x=layers.Dense(64, activation="relu")(x)
#
#    x=layers.Dense(64, activation="relu")(x)
#

    x = layers.Dense(64)(x)
    norm_embeddings = tf.nn.l2_normalize(x, axis=-1)
    embedding_network = keras.Model(input, norm_embeddings)
    input_1 = layers.Input((20, 657, 1))
    input_2 = layers.Input((20, 657, 1))


    tower_1 = embedding_network(input_1)
    tower_2 = embedding_network(input_2)


    merge_layer = layers.Lambda(euclidean_distance)([tower_1, tower_2])
#    normal_layer = tf.keras.layers.BatchNormalization()(merge_layer)
  # output_layer = layers.Dense(1, activation="sigmoid")(merge_layer)
    siamese = keras.Model(inputs=[input_1, input_2], outputs=merge_layer)

    siamese.compile(loss=loss(margin),optimizer=Adam(lr=0.001), metrics=["accuracy"])
    siamese.summary()



    history=siamese.fit([X1, X2], y, epochs=50, batch_size=8, verbose=False,validation_data=([test_X1, test_X2], test_y))
    embedding_network.save(os.getcwd()+"/singleoverlap1234test.h5")
    siamese.save(os.getcwd()+"/siamese_modeloverlap1234test.h5")
    hist_df = pd.DataFrame(history.history)


    hist_json_file = 'historyoverlap1234test.json'
    with open(hist_json_file, mode='w') as f:
        hist_df.to_json(f)

    hist_csv_file = 'historyoverlap1234test.csv'
    with open(hist_csv_file, mode='w') as f:
        hist_df.to_csv(f)

    plt_metric(history=history.history, metric="accuracy", title="Model accuracy")
    plt_metric(history=history.history, metric="loss", title="Constrastive Loss")
    return embedding_network,siamese

X=np.load('train_augumented_mfcc_vectorsoverlapsirprep.npy')
y=np.load('train_augumented_labelsoverlapsir71prep.npy')

healthy = X[np.where(y==0)]
print(healthy.shape)
mild = X[np.where(y==3)]
print(mild.shape)
moderate = X[np.where(y==4)]
print(moderate.shape)


healthy= shuffle(healthy, random_state=0)
moderate= shuffle(moderate, random_state=0)
mild = shuffle(mild, random_state=0)

test_healthy = healthy[200:300]
test_mild = mild[200:300]
test_moderate = moderate[200:300]
np.save('healthytest114test.npy',healthy[300:400])
np.save('mildtest114test.npy',mild[300:400])
np.save('moderatetest114test.npy',moderate[300:400])

healthy = healthy[:200]
mild = mild[:200]
moderate = moderate[:200]


positive_healthy = list(itertools.combinations(healthy, 2))

positive_moderate = list(itertools.combinations(moderate, 2))

positive_mild = list(itertools.combinations(mild, 2))



test_positive_healthy = list(itertools.combinations(test_healthy, 2))

test_positive_moderate = list(itertools.combinations(test_moderate, 2))

test_positive_mild = list(itertools.combinations(test_mild, 2))


negative1 = itertools.product(healthy,mild)
negative1 = list(negative1)

negative2 = itertools.product(mild,moderate)
negative2 = list(negative2)

negative3 = itertools.product(healthy,moderate)
negative3 = list(negative3)


test_negative1 = itertools.product(test_healthy,test_mild)
test_negative1 = list(test_negative1)

test_negative2 = itertools.product(test_mild,test_moderate)
test_negative2 = list(test_negative2)

test_negative3 = itertools.product(test_healthy,test_moderate)
test_negative3 = list(test_negative3)

modelinput1 = []
modelinput2 = []
modeloutput = []
test_modelinput1 = []
test_modelinput2 = []
test_modeloutput = []
positive_samples = positive_moderate + positive_mild + positive_healthy
negative_samples = negative1 + negative2 + negative3
negative_samples= shuffle(negative_samples, random_state=0)

k= len(positive_samples)
n = len(negative_samples)
print(len(positive_samples))
# for i in range(0, n - k ):
#     negative_samples.pop()

test_positive_samples = test_positive_moderate + test_positive_mild + test_positive_healthy

test_negative_samples = test_negative1 + test_negative2 + test_negative3
test_negative_samples= shuffle(test_negative_samples, random_state=0)
print(len(test_positive_samples))
k= len(test_positive_samples)
n = len(test_negative_samples)

for fname in positive_samples :
    im = fname[0]
    modelinput1.append(im)
    im = fname[1]
    modelinput2.append(im)
    modeloutput.append(1)

for fname in test_positive_samples :
    im = fname[0]
    test_modelinput1.append(im)
    im = fname[1]
    test_modelinput2.append(im)
    test_modeloutput.append(1)



for fname in negative_samples :
    im = fname[0]
    modelinput1.append(im)
    im = fname[1]
    modelinput2.append(im)
    modeloutput.append(0)

for fname in test_negative_samples :
    im = fname[0]
    test_modelinput1.append(im)
    im = fname[1]
    test_modelinput2.append(im)
    test_modeloutput.append(0)
del X
del y


modeloutputa = np.array(modeloutput).astype("float32")


modelinput1a = np.array(modelinput1)


modelinput2a = np.array(modelinput2)
test_modeloutputa = np.array(test_modeloutput).astype("float32")


test_modelinput1a = np.array(test_modelinput1)
test_modelinput2a = np.array(test_modelinput2)


modelinput1a = modelinput1a.reshape((len(negative_samples) + len(positive_samples), 20, 657, 1))
modelinput2a = modelinput2a.reshape((len(negative_samples) + len(positive_samples), 20, 657, 1))


test_modelinput1a = test_modelinput1a.reshape((len(test_negative_samples) + len(test_positive_samples), 20, 657, 1))
test_modelinput2a = test_modelinput2a.reshape((len(test_negative_samples) + len(test_positive_samples), 20, 657, 1))

del test_negative_samples
del test_positive_samples
del positive_samples
del negative_samples

margin=1
from sklearn.utils import shuffle
modelinput1a, modelinput2a, modeloutputa = shuffle(modelinput1a, modelinput2a, modeloutputa, random_state=0)
test_modelinput1a, test_modelinput2a, test_modeloutputa = shuffle(test_modelinput1a, test_modelinput2a, test_modeloutputa, random_state=0)


single_model(modelinput1a, modelinput2a, modeloutputa,test_modelinput1a, test_modelinput2a, test_modeloutputa)



