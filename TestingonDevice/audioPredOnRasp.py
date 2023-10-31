import os, psutil, sys, matplotlib.pyplot as plt, numpy as np, itertools, math
from random import seed
from random import random, randint
from scipy.spatial import distance
import pickle, csv
import random
import warnings
from math import factorial
from gpiozero import CPUTemperature
import tensorflow
from tensorflow import keras

#def getFreeDescription():
#    free = os.popen("free -h")
#    i = 0
#    while True:
#        i = i + 1
#        line = free.readline()
#        if i == 1:
#            return (line.split()[0:7])
#
#
#def getFree():
#    free = os.popen("free -h")
#    i = 0
#    while True:
#        i = i + 1
#        line = free.readline()
#        if i == 2:
#            return (line.split()[0:7])
#
#def printResult(i,pred,prob):
#    result=[]
#    result.append(pred)
#    result.append(prob)
#    result.insert(0,i)
##    print(description[0] + " : " + mem[1])
##    print(description[1] + " : " + mem[2])
##    print(description[2] + " : " + mem[3])
##    print(description[3] + " : " + mem[4])
##    print(description[4] + " : " + mem[5])
##    print(description[5] + " : " + mem[6])
#    with open(r'results1.csv', 'a') as f:
#        writer = csv.writer(f)
#        writer.writerow(result)
#
#def printPerformance(epochs,t):
#    cpu = CPUTemperature()
#
#    print("temperature: " + str(cpu.temperature))
#    CPU_Pct=str(round(float(os.popen('''grep 'cpu ' /proc/stat | awk '{usage=($2+$4)*100/($2+$4+$5)} END {print usage }' ''').readline()),2))
#
#    #print results
#    print("CPU Usage = " + CPU_Pct)
#    CPU=[]
#    time=[]
#    mem=[]
#    CPU.append(CPU_Pct)
#    time.append(t)
#    description = getFreeDescription()
#    process = psutil.Process(os.getpid())
#    print(process.memory_info().rss)
#    mem.append(process.memory_info().rss)
#    mem.insert(0,epochs)
#    CPU.insert(0,epochs)
#    time.insert(0,epochs)
##    print(description[0] + " : " + mem[1])
##    print(description[1] + " : " + mem[2])
##    print(description[2] + " : " + mem[3])
##    print(description[3] + " : " + mem[4])
##    print(description[4] + " : " + mem[5])
##    print(description[5] + " : " + mem[6])
#    with open(r'mem2.csv', 'a') as f:
#        writer = csv.writer(f)
#        writer.writerow(mem)
#    with open(r'cpu2.csv', 'a') as f:
#        writer = csv.writer(f)
#        writer.writerow(CPU)
#    with open(r'roundtime2.csv', 'a') as f:
#        writer = csv.writer(f)
#        writer.writerow(time)


basepath = '/home/pi/piffles'
def wav2mfcc(wave, max_len=188):
#     mfcc = librosa.feature.mfcc(wave, sr=16000)
    try:
        import librosa 
        mfcc = librosa.feature.mfcc(wave, n_mfcc=60, sr=48000)
    except:
        pass
    print("audio converted to mfcc...")
#     print(mfcc.shape)

    # If maximum length exceeds mfcc lengths then pad the remaining ones
    if (max_len > mfcc.shape[1]):
        pad_width = max_len - mfcc.shape[1]
        mfcc = np.pad(mfcc, pad_width=((0, 0), (0, pad_width)), mode='constant')

    # Else cutoff the remaining parts
    else:
        mfcc = mfcc[:, :max_len]
    
    return mfcc

import numpy as np
import pyaudio
import time

X=np.load('train_augumented_mfcc_vectorsoverlap3new.npy')
y=np.load('train_augumented_labelsoverlap3new.npy')
red_im = X[np.where(y==0)]
print(red_im.shape)
blue_im = X[np.where(y==1)]
print(blue_im.shape)
green_im = X[np.where(y==2)]
print(green_im.shape)
np.random.shuffle(red_im)
np.random.shuffle(green_im)
np.random.shuffle(blue_im)
# np.random.shuffle(yellow_im)

# Test images
test_red_im = red_im[1:20]
test_blue_im = blue_im[1:20]
test_green_im = green_im[1:20]
#
#
#        if (max_len > mfcc.shape[1]):
#                pad_width = max_len - mfcc.shape[1]
#                mfcc = np.pad(mfcc, pad_width=((0, 0), (0, pad_width)), mode='constant')
#
#        # Else cutoff the remaining parts
#        else:
#            mfcc = mfcc[:, :max_len]
#
#        mfcc = np.expand_dims(mfcc, axis=0)
#        mfcc = np.expand_dims(mfcc, axis=3)
#        for i in range(0,3):
#            for j in range(5):
#                x=random.choice(os.listdir(basepath+"/"+str(i)))
#                y=random.choice(os.listdir(basepath+"/"+str(i)))
#                try:
#                    with open(basepath+"/"+str(i)+"/"+x, "rb") as fp:   # Unpickling
#
#                        b = pickle.load(fp)
#
#                except:
#                    pass
#                try:
#                    with open(basepath+"/"+str(i)+"/"+y, "rb") as fp:   # Unpickling
#
#                        b1 = pickle.load(fp)
#
#                except:
#                    pass
#
#                print("Loaded Covid status-anchor audios")
#                import tensorflow
#                from tensorflow import keras
siamese = keras.models.load_model('color_siamese_modeloverlap3 (1).h5',compile=False)
#
#                b = np.expand_dims(b, axis=0)
#
#
#                b = np.expand_dims(b, axis=3)
#
#                b1 = np.expand_dims(b1, axis=0)
#
#
#                b1 = np.expand_dims(b1, axis=3)
   
#            predictions = siamese.predict([mfcc, b])

for x,y in zip(test_red_im,test_red_im[1:]):

    x = np.expand_dims(x, axis=0)
    x = np.expand_dims(x, axis=3)
    y = np.expand_dims(y, axis=0)
    y = np.expand_dims(y, axis=3)

    num1 = siamese.predict([x, y])[0][0]
    print("predictions",num1)

    if(num1>0.5):
        largest = 1
    else:
        largest = 0

    printResult(1,largest,num1)
    
test_red_im = red_im[21:40]
test_blue_im = blue_im[21:40]
test_green_im = green_im[21:40]

for x,y in zip(test_red_im,test_blue_im):

    x = np.expand_dims(x, axis=0)
    x = np.expand_dims(x, axis=3)
    y = np.expand_dims(y, axis=0)
    y = np.expand_dims(y, axis=3)

    num1 = siamese.predict([x, y])[0][0]
    print("predictions",num1)

    if(num1>0.5):
        largest = 1
    else:
        largest = 0

    printResult(0,largest,num1)
    
    
test_red_im = red_im[41:60]
test_blue_im = blue_im[41:60]
test_green_im = green_im[41:60]
    
for x,y in zip(test_blue_im,test_blue_im[1:]):

    x = np.expand_dims(x, axis=0)
    x = np.expand_dims(x, axis=3)
    y = np.expand_dims(y, axis=0)
    y = np.expand_dims(y, axis=3)

    num1 = siamese.predict([x, y])[0][0]
    print("predictions",num1)

    if(num1>0.5):
        largest = 1
    else:
        largest = 0

    printResult(1,largest,num1)
    
test_red_im = red_im[61:80]
test_blue_im = blue_im[61:80]
test_green_im = green_im[61:80]

for x,y in zip(test_blue_im,test_green_im):

    x = np.expand_dims(x, axis=0)
    x = np.expand_dims(x, axis=3)
    y = np.expand_dims(y, axis=0)
    y = np.expand_dims(y, axis=3)

    num1 = siamese.predict([x, y])[0][0]
    print("predictions",num1)

    if(num1>0.5):
        largest = 1
    else:
        largest = 0

    printResult(0,largest,num1)
    
test_red_im = red_im[81:100]
test_blue_im = blue_im[81:100]
test_green_im = green_im[81:100]
for x,y in zip(test_green_im,test_green_im[1:]):

    x = np.expand_dims(x, axis=0)
    x = np.expand_dims(x, axis=3)
    y = np.expand_dims(y, axis=0)
    y = np.expand_dims(y, axis=3)

    num1 = siamese.predict([x, y])[0][0]
    print("predictions",num1)

    if(num1>0.5):
        largest = 1
    else:
        largest = 0

    printResult(1,largest,num1)
    
test_red_im = red_im[101:120]
test_blue_im = blue_im[101:120]
test_green_im = green_im[101:120]
for x,y in zip(test_green_im,test_blue_im):

    x = np.expand_dims(x, axis=0)
    x = np.expand_dims(x, axis=3)
    y = np.expand_dims(y, axis=0)
    y = np.expand_dims(y, axis=3)

    num1 = siamese.predict([x, y])[0][0]
    print("predictions",num1)

    if(num1>0.5):
        largest = 1
    else:
        largest = 0

    printResult(0,largest,num1)
    
test_red_im = red_im[1:20]
test_blue_im = blue_im[1:20]
test_green_im = green_im[1:20]
    
for x,y in zip(test_blue_im,test_blue_im[1:]):

    x = np.expand_dims(x, axis=0)
    x = np.expand_dims(x, axis=3)
    y = np.expand_dims(y, axis=0)
    y = np.expand_dims(y, axis=3)

    num1 = siamese.predict([x, y])[0][0]
    print("predictions",num1)

    if(num1>0.5):
        largest = 1
    else:
        largest = 0

    printResult(1,largest,num1)
    
    
test_red_im = red_im[21:40]
test_blue_im = blue_im[21:40]
test_green_im = green_im[21:40]

for x,y in zip(test_blue_im,test_red_im):

    x = np.expand_dims(x, axis=0)
    x = np.expand_dims(x, axis=3)
    y = np.expand_dims(y, axis=0)
    y = np.expand_dims(y, axis=3)

    num1 = siamese.predict([x, y])[0][0]
    print("predictions",num1)

    if(num1>0.5):
        largest = 1
    else:
        largest = 0

    printResult(0,largest,num1)

test_red_im = red_im[41:60]
test_blue_im = blue_im[41:60]
test_green_im = green_im[41:60]

for x,y in zip(test_red_im,test_red_im[1:]):

    x = np.expand_dims(x, axis=0)
    x = np.expand_dims(x, axis=3)
    y = np.expand_dims(y, axis=0)
    y = np.expand_dims(y, axis=3)

    num1 = siamese.predict([x, y])[0][0]
    print("predictions",num1)

    if(num1>0.5):
        largest = 1
    else:
        largest = 0

    printResult(1,largest,num1)
    
    

#class AudioHandler(object):
#    def __init__(self):
#        self.FORMAT = pyaudio.paFloat32
#        self.CHANNELS = 1
#        self.RATE = 48000
#        self.CHUNK = 1024 * 2
#        self.p = None
#        self.stream = None
#
#    def start(self):
#        self.p = pyaudio.PyAudio()
#        self.stream = self.p.open(format=self.FORMAT,
#                                  channels=self.CHANNELS,
#                                  rate=self.RATE,
#                                  input=True,
#                                  output=False,
#                                  stream_callback=self.callback,
#                                  frames_per_buffer=self.CHUNK)
#
#    def stop(self):
#        self.stream.close()
#        self.p.terminate()
#
#    def callback(self, in_data, frame_count, time_info, flag):
#        max_len=188
#
#        t = time.time()
##        printPerformance("init",t)
#        numpy_array = np.frombuffer(in_data, dtype=np.float32)
#
#
#        mfcc=wav2mfcc(numpy_array,max_len)
#
#
#        if (max_len > mfcc.shape[1]):
#                pad_width = max_len - mfcc.shape[1]
#                mfcc = np.pad(mfcc, pad_width=((0, 0), (0, pad_width)), mode='constant')
#
#        # Else cutoff the remaining parts
#        else:
#            mfcc = mfcc[:, :max_len]
#
#        mfcc = np.expand_dims(mfcc, axis=0)
#        mfcc = np.expand_dims(mfcc, axis=3)
#        for i in range(0,3):
#            for j in range(5):
#                x=random.choice(os.listdir(basepath+"/"+str(i)))
#                y=random.choice(os.listdir(basepath+"/"+str(i)))
#                try:
#                    with open(basepath+"/"+str(i)+"/"+x, "rb") as fp:   # Unpickling
#
#                        b = pickle.load(fp)
#
#                except:
#                    pass
#                try:
#                    with open(basepath+"/"+str(i)+"/"+y, "rb") as fp:   # Unpickling
#
#                        b1 = pickle.load(fp)
#
#                except:
#                    pass
#
#                print("Loaded Covid status-anchor audios")
#                import tensorflow
#                from tensorflow import keras
#                siamese = keras.models.load_model('color_siamese_modeloverlap3 (1).h5',compile=False)
#
#                b = np.expand_dims(b, axis=0)
#
#
#                b = np.expand_dims(b, axis=3)
#
#                b1 = np.expand_dims(b1, axis=0)
#
#
#                b1 = np.expand_dims(b1, axis=3)
#
#    #            predictions = siamese.predict([mfcc, b])
#                predictions = siamese.predict([b1, b])
#                print("predictions",predictions)
#                t = time.time()  #store end time
#    #            printPerformance(i,t)
#                printResult(i,predictions)
#
#
#
#        return None, pyaudio.paContinue
#
#    def mainloop(self):
##        while (self.stream.is_active()): # if using button you can set self.stream to 0 (self.stream = 0), otherwise you can use a stop condition
##            time.sleep(2.0)
#
#
#audio = AudioHandler()
#audio.start()     # open the the stream
#audio.mainloop()  # main operations with librosa
#audio.stop()

