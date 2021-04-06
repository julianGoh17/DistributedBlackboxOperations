#!/bin/bash

docker stop cadvisor || true && docker rm cadvisor || true
