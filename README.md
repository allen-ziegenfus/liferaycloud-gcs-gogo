# liferaycloud-gcs-gogo 

This is a simple example of a Gogo Shell command for inspecting the contents of a liferaycloud GCS bucket.

## Deploying: 

Download the built jar file (e.g. <a href="https://github.com/allen-ziegenfus/liferaycloud-gcs-gogo/actions/runs/14528082627/artifacts/2967840714">workflow artifact</a>) and deploy to the /opt/liferay/deploy directory. After it deploys and resolves there should be new gogo shell commands available. 

### There are two commands:

- countFiles - print a summary of files and their sizes
- listFiles - print out all the files in the bucket

Note that this has only been tested with small numbers of document library files. For navigating larger document libaries the code could be modified to run in the background.

### Example output:
<pre>
g! countFiles
Bucket: doclib-rbmrbiedzzzttsugrc-56f03200-f36c-4987-a154-342b95b8c8c3
Number of .index files: 1, total size: 64738
Number of duplicated files: 0, total size: 0
Number of adaptive files: 98, total size: 5607376
Number of document_thumbnail files: 1, total size: 65426
Number of document_preview files: 3, total size: 217828
Number of regular files: 80, total size: 12583262
g! 
  </pre>

## Compatibility: 

This module was tested with Liferay DXP 7.2, 7.3, 7.4 and 2025.q1.8 
