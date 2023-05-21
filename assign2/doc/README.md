<a name="readme-top"></a>


# CPD-Project 2nd Project
Project made during the course of Parallel and Distributed Computing (CPD) in FEUP.



<!-- TABLE OF CONTENTS -->
## Table of Contents
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#authors">Authors</a></li>
  </ol>



<!-- ABOUT THE PROJECT -->
## About The Project

<!-- ![alt text](https://cdn.tutsplus.com/gamedev/uploads/2013/08/img1_server_client.png) -->

We were just hired for an internet online game company and our first task was to create a client-server system using TCP sockets in Java. The company has users that can authenticate with the system in order to play some text based game among them. 

Users connect to the system using some Java client code that allows users to first authenticate and then enter a queue for the game and finally play the game until it completes.

The server needs to accept and authenticate user connections, and make teams of n users for the game. Once the team is formed a thread can be requested from a thread pool (e.g. Executors.newFixedThreadPool(5);) to create and animate a new Game instance. 

The configuration of the thread pool defines the maximum number of games that can run concurrently in the server.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



### Built With

<img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white" alt="Java">

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- GETTING STARTED -->
## Getting Started

Instructions on setting up the project locally:

### Run

1. Compile all the required Java files. In src directory, run in a terminal:
    ```sh
   javac **/*.java
   ```
3. Run the server in a terminal in src directory:
   ```sh
   java Launcher -s <mode> <port>
   ```
   mode: 1
   
   port: desired port (e.g. 8000)
   
3. Run the client in a terminal in src directory: 
   ```sh
   java Launcher -c <hostname> <port>
   ```
   hostname: localhost
   
   port: same port as server (e.g. 8000)

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- CONTACT -->
## Authors

Gonçalo Jorge Soares Ferreira - up202004761@fe.up.pt

Gonçalo José Domingues - up202007914@fe.up.pt

Pedro Nuno Ferreira Moura de Macedo - up202007531@fe.up.pt

<p align="right">(<a href="#readme-top">back to top</a>)</p>
