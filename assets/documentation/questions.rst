=========
Questions
=========

Parser pauses when low_memory=false
-----------------------------------

So if you have low_memory=false then what happens is that searchcode will add processed files into a queue which occasionally is flushed to disk in a single transaction. By contrast when low_memory is set true each one is written to disk when processed, which is slower (lots more disk IO) but since there is no buffering uses less memory.

The queue needs to be in place because with enough repository processors or complex files the indexer can lag behind the number of things to index.

You can tweak these values that control the size of the queue using the properties file.

.. code-block:: bash

	max_document_queue_size=1000
	max_document_queue_line_size=100000
	max_file_line_depth=10000
	index_queue_batch_size=1000

The first is the maximum number of files that will be added to the queue because the indexer pauses. By default it is set to 1,000 files.

The second is the number of lines in those files. A count is kept of the number of lines that are in each file in the queue and when the sum of them is over the value the indexer is paused. Please note that this is a soft cap that can be breached a few times before the pause kicks in.

The third is the number of lines that searchcode will read into a file for indexing. Generally file over 10,000 lines are not that useful for search results and just slow things down. This value dictates how much the soft cap of max_document_queue_line_size can be breached.

The final value is the number of queued batch items that will be written out to the index in a single transaction. When indexing happens searchcode will pop this many items from the internal queue then index them and write the index to disk.

If you have enough memory on the machine (and this is a case by case basis) you can increase the first two to have a larger buffer. When the indexer is busy and falling behind that could be because it ran into a bunch of complex/large files which cause it to do more work. However once it runs into a batch of files that it can work on quickly this can cause it to work on larger chunks for indexing. It may also be worthwhile in these situations to increase the value of index_queue_batch_size to match whatever the max_document_queue_size value is.