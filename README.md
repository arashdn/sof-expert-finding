# My Research on StackOverflow

This repository contains source codes developed for my reaserach on StackOverflow


### Published Papers:
1.

	Arash Dargahi Nobari, Sajad Sotudeh Gharebagh and Mahmood Neshati. “Skill Translation Models in Expert Finding”,
	In proceedings of The 40th International ACM SIGIR Conference on Research and Development in Information Retrieval (SIGIR ’17), Aug 2017.

You may check the [paper](http://dl.acm.org/citation.cfm?id=3080719) ([PDF](http://facultymembers.sbu.ac.ir/neshati/wp-content/uploads/2015/03/Skill-Translation-Models-in-Expert-Finding.pdf)) for more information.


## Requirements

JDK8 and Apache Lucene 6.2.1 is required for running the code.

To run machine learning algorithms there is another [repository](https://github.com/arashdn/sof-expert-finding-ml) in python with Tensorflow

## Data

All of data(including test collection, goldens, etc) and libraries are ignored in git repository.

These files can be downloaded from [dropbox](https://www.dropbox.com/s/kwi6gog15684shd/data_java.zip) This file includes two folders, `data` and `lib`

The `lib` folder includes all libraries(including [Apache Lucene](https://lucene.apache.org) and [jsoup](https://jsoup.org)) required to run the project

The `data` folder includes the following files and folders:

- `golden`: The golden collection described in the paper.
- `java_a_tag.txt`: Tags for each answer (Answers don't have tag by their self, taged are extracted from related questions)
- `java_q_tag.txt`: Tags for each question.
- `Q_A.txt`: each question and it's answer ids.
- `Posts.xml`: This file is removed from data(due to it's very large size), This is the main dataset obtained from [archive.org](https://archive.org/details/stackexchange) including posts from 2008-07-31 until 2015-03-08 at the time we download it. the version used in our paper can be downloaded [here](http://files.arashdargahi.com/sof_posts_2015/)


## Usage
Codes include a simple GUI to be used more easily.

Add `data` folder to the project and put Posts.xml inside it.
Then Index posts using the button. All other functions have their own button.

## Citation

Please cite the paper, If you used the codes in this repository.
